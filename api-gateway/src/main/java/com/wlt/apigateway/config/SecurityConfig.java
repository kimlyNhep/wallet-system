package com.wlt.apigateway.config;

import com.wlt.apigateway.filter.JwtAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {

        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.authorizeExchange(it -> it
                .pathMatchers("/api/auth/v1/user/register").permitAll()
                .pathMatchers("/api/auth/v1/login").permitAll()
                .pathMatchers("/api/payment/v1/protected/gift-code/generate").hasAuthority("GEN_GIFT_CODE")
                .anyExchange().authenticated());
        http.addFilterBefore(new JwtAuthorizationFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }
}
