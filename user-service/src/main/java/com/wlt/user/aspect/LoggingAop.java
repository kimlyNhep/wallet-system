package com.wlt.user.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LoggingAop {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAop.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {

    }

    @Before("restControllerMethods()")
    public void logBeforeServiceMethod(JoinPoint joinPoint) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("action", "enter");

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Map<String, String[]> requestParamsMap = request.getParameterMap();
                Map<String, Object> multiValueParams = new HashMap<>();
                for (Map.Entry<String, String[]> entry : requestParamsMap.entrySet()) {
                    multiValueParams.put(entry.getKey(), Arrays.asList(entry.getValue()));
                }
                logData.put("requestParams", multiValueParams);
                logData.put("method", request.getMethod());
                logData.put("uri", request.getRequestURI());
            } else {
                logData.put("request", "No HttpServletRequest available");
            }

            String jsonLog = objectMapper.writeValueAsString(logData);
            logger.info(jsonLog);

        } catch (Exception e) {
            logger.error("Error during JSON logging for method entry: {}", joinPoint.getSignature().toShortString(), e);
        }
    }
}
