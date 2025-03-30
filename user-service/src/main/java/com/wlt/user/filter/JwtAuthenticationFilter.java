package com.wlt.user.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wlt.user.entity.Session;
import com.wlt.user.entity.User;
import com.wlt.user.repository.SessionRepository;
import com.wlt.user.repository.UserRepository;
import com.wlt.user.service.impl.CustomUserDetail;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepository sessionRepository;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String username = request.getParameter("email");
        String password = request.getParameter("password");

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication)
            throws IOException {
        CustomUserDetail user = (CustomUserDetail) authentication.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());

        Optional<User> userInfo = userRepository.findByEmail(user.getUsername());
        if (userInfo.isEmpty()) {
            throw new RuntimeException("user not found");
        }

        String ipAddress = getClientIpAddress(request);
        // session if there is active session or not

        String userAgent = request.getHeader("User-Agent");
        Optional<Session> currentSessionOptional = sessionRepository.findByUserIdAndStatus(userInfo.get().getId(), "ACT");
        LocalDateTime now = LocalDateTime.now();

        if (currentSessionOptional.isPresent()) {
            if (currentSessionOptional.get().getExpiresAt().isAfter(now)) {
                Session currentSession = currentSessionOptional.get();
                if (!currentSession.getIp().equals(ipAddress) || !currentSession.getUserAgent().equals(userAgent)) {
                    throw new RuntimeException("user already logged in other device");
                }
            }
        }

        String accessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 100 * 60 * 1000))
                .withIssuer(request.getRequestURI())
                .withClaim("userId", userInfo.get().getId())
                .withClaim("roles", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .sign(algorithm);

        //1. if success we create session
        if (currentSessionOptional.isEmpty()) {
            Session session = new Session();
            session.setIp(ipAddress);
            session.setToken(passwordEncoder.encode(accessToken));
            session.setUserId(userInfo.get().getId());
            session.setStatus("ACT");
            session.setExpiresAt(LocalDateTime.now().plusHours(10));
            session.setUserAgent(userAgent);
            sessionRepository.save(session);
        } else {
            if (currentSessionOptional.get().getExpiresAt().isAfter(now)) {
                currentSessionOptional.get()
                        .setToken(passwordEncoder.encode(accessToken));
                currentSessionOptional.get().setIp(ipAddress);
                currentSessionOptional.get().setExpiresAt(LocalDateTime.now().plusHours(10));
                sessionRepository.save(currentSessionOptional.get());
            }
        }


        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        // Check headers for proxies (e.g., Nginx, Cloudflare)
        String ipAddress = request.getHeader("X-Forwarded-For");

        // Fallback for other proxy headers
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        // Default to remote address
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // Handle multiple IPs in X-Forwarded-For
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Username or Password is invalid");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), errors);
    }
}