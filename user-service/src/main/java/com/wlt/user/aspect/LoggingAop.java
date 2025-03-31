package com.wlt.user.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LoggingAop {
    @Around("execution(* com.wlt.user..*(..))")
    public Object logAnyMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String[] parameterNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        Map<String, Object> requestData = new HashMap<>();

        /// Loop through each params and extract its name and value
        for (int i = 0; i < parameterNames.length; i++) {
            String paramName = parameterNames[i];
            if (paramName.equals("codeSignature") ||
                    paramName.equals("context") ||
                    paramName.equals("typeReference")
                    || joinPoint.getArgs()[i] instanceof BindingResult) {
                continue;
            }
            requestData.put(paramName, joinPoint.getArgs()[i]);
        }

        logAsJson(joinPoint, requestData, "Enter");

        Object responseData = joinPoint.proceed();

        logAsJson(joinPoint, responseData, "Exit");

        return responseData;
    }

    private void logAsJson(ProceedingJoinPoint joinPoint, Object data, String action) {
        String method = joinPoint.getSignature().toShortString();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.registerModule(new Jdk8Module());
            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("action", action);
            userNode.put("method", method);
            userNode.put("data", new ObjectMapper().writeValueAsString(data));
            // Check if data is a Spring CGLIB proxy
            if (data != null && Enhancer.isEnhanced(data.getClass())) {
                log.warn("CGLIB Proxy detected - skipping serialization");
            }
            else {
                String json = objectMapper.writeValueAsString(userNode);
                log.info(json);
            }
        }
        catch (Exception e) {
            log.warn(e.getMessage());
        }
    }
}
