package com.personal.aoppoc.aspect;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.personal.aoppoc.annotation.LogEndpoint;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EndpointLoggingAspect {
    ObjectMapper objectMapper;

    public EndpointLoggingAspect() {
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Before("@annotation(logEndpoint)")
    public void logEndpointInputs(final JoinPoint joinPoint, final LogEndpoint logEndpoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn("Could not get request attributes for logging");
                return;
            }

            final HttpServletRequest request = attributes.getRequest();

            final Map<String, Object> logData = new LinkedHashMap<>();
            logData.put("method", request.getMethod());
            logData.put("uri", request.getRequestURI());
            logData.put("endpoint", joinPoint.getSignature().getName());


            if (logEndpoint.logHeaders()) {
                logData.put("headers", getFilteredHeaders(request, logEndpoint.excludeHeaders()));
            }


            if (logEndpoint.logParams()) {
                logData.put("parameters", getRequestParameters(request, logEndpoint.excludeParams()));
            }


            if (logEndpoint.logPathVariables()) {
                logData.put("pathVariables", getPathVariables(joinPoint, logEndpoint.excludePathVariables()));
            }


            if (logEndpoint.logBody()) {
                logData.put("requestBody", getRequestBody(joinPoint));
            }

            log.info("Endpoint Request: {}", objectMapper.writeValueAsString(logData));

        } catch (Exception e) {
            log.error("Error logging endpoint inputs", e);
        }
    }

    private Map<String, String> getFilteredHeaders(final HttpServletRequest request, final String[] excludeHeaders) {
        final Set<String> excludeSet = Arrays.stream(excludeHeaders)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        final Map<String, String> headers = new HashMap<>();
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            if (!excludeSet.contains(headerName.toLowerCase())) {
                headers.put(headerName, request.getHeader(headerName));
            }
        });
        return headers;
    }

    private Map<String, String[]> getRequestParameters(HttpServletRequest request, String[] excludeParams) {
        final Set<String> excludeSet = Arrays.stream(excludeParams)
                .collect(Collectors.toSet());

        return request.getParameterMap().entrySet().stream()
                .filter(entry -> !excludeSet.contains(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Map<String, Object> getPathVariables(final JoinPoint joinPoint, final String[] excludePathVariables) {
        final Set<String> excludeSet = Arrays.stream(excludePathVariables)
                .collect(Collectors.toSet());

        final Map<String, Object> pathVariables = new HashMap<>();

        final Object[] args = joinPoint.getArgs();
        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        final Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length && i < args.length; i++) {
            PathVariable pathVarAnnotation =
                    parameters[i].getAnnotation(PathVariable.class);

            if (!ObjectUtils.isEmpty(pathVarAnnotation)) {
                String paramName = pathVarAnnotation.value().isEmpty() ?
                        pathVarAnnotation.name().isEmpty() ? parameters[i].getName() : pathVarAnnotation.name() :
                        pathVarAnnotation.value();
                if (!excludeSet.contains(paramName.toLowerCase())) {
                    pathVariables.put(paramName, args[i]);

                }
            }
        }

        return pathVariables;
    }

    private Object getRequestBody(final JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Method method = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length && i < args.length; i++) {
            if (parameters[i].isAnnotationPresent(org.springframework.web.bind.annotation.RequestBody.class)) {
                return args[i];
            }
        }

        return null;
    }
}

