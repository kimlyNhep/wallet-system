package com.wlt.apigateway.config;

import com.wlt.apigateway.filter.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${user.service.base-url}")
    private String userServiceUrl;

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {

        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.authorizeExchange(it -> it
                .pathMatchers("/api/auth/v1/user/register").permitAll()
                .pathMatchers("/api/auth/v1/login").permitAll()
                .pathMatchers("/api/payment/v1/protected/gift-code/generate")
                    .hasAuthority("GEN_GIFT_CODE")
                .anyExchange().authenticated());
        http.addFilterBefore(new JwtAuthorizationFilter(userServiceUrl, restTemplate()), SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new LongPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
