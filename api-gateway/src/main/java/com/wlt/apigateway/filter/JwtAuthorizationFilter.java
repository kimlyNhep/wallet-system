package com.wlt.apigateway.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wlt.apigateway.dto.SessionResponseDto;
import com.wlt.apigateway.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
public class JwtAuthorizationFilter implements WebFilter {

    private final String userServiceUrl;
    private final RestTemplate restTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);

        // validate token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(token);
                String username = decodedJWT.getSubject();
                String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                Arrays.stream(roles).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
                Long userId = decodedJWT.getClaim("userId").asLong();

                // check session by token
                MultiValueMap<String, String> headers = new HttpHeaders();
                headers.add("Collation-id", UUID.randomUUID().toString());
                headers.add("user-id", String.valueOf(userId));
                headers.add("token", token);

                ResponseEntity<SuccessResponse<SessionResponseDto>> currentSession = restTemplate.exchange(
                        userServiceUrl + "/api/v1/session",
                        HttpMethod.GET,
                        new HttpEntity<>(null, headers),
                        new ParameterizedTypeReference<>() {
                        }
                );

                if (currentSession.getStatusCode().is2xxSuccessful()) {
                    SuccessResponse<SessionResponseDto> sessionResponseDto = currentSession.getBody();
                    if (sessionResponseDto != null && sessionResponseDto.getResponse() != null) {
                        SessionResponseDto session = sessionResponseDto.getResponse();
                        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
                            throw new RuntimeException("Session expired");
                        }
                    } else {
                        throw new RuntimeException("Invalid token");
                    }
                }

                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("user-id", String.valueOf(userId))
                        .header("roles", String.join(",", roles))
                        .header("token", token)
                        .build();

                ServerWebExchange newExchange = exchange.mutate().request(request).build();
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);

                return chain.filter(newExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticationToken));

            } catch (Exception e) {
                exchange.getResponse().getHeaders().add("error", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                Map<String, String> error = new HashMap<>();
                error.put("error_message", e.getMessage());

                DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    byte[] errorBytes = objectMapper.writeValueAsBytes(error);
                    DataBuffer dataBuffer = bufferFactory.wrap(errorBytes);
                    return exchange.getResponse().writeWith(Mono.just(dataBuffer));
                } catch (IOException ex) {
                    // Handle serialization error
                    return Mono.error(ex);
                }
            }
        } else {
            return chain.filter(exchange);
        }
    }
}
