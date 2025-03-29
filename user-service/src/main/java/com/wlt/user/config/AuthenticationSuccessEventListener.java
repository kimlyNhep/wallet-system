package com.wlt.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationSuccessEventListener.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        try {
            Authentication authentication = event.getAuthentication();
            Map<String, Object> logData = new HashMap<>();
            logData.put("action", "authentication_success");
            logData.put("username", authentication.getName());
            logData.put("principal", authentication.getPrincipal()); // You might want to log more details here

            String jsonLog = objectMapper.writeValueAsString(logData);
            logger.info(jsonLog); // Log successful authentication at INFO level

        } catch (Exception e) {
            logger.error("Error logging authentication success event: {}", e.getMessage(), e);
        }
    }
}
