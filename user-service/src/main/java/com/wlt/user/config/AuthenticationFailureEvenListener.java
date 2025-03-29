package com.wlt.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthenticationFailureEvenListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationFailureEvenListener.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("action", "authentication_failure");
            logData.put("username", event.getAuthentication().getName());
            logData.put("error", event.getException().getMessage());

            String jsonLog = objectMapper.writeValueAsString(logData);
            logger.warn(jsonLog); // Log authentication failures at WARN level

        } catch (Exception e) {
            logger.error("Error logging authentication failure event: {}", e.getMessage(), e);
        }
    }
}
