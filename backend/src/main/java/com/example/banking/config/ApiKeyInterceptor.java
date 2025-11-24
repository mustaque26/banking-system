package com.example.banking.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Value("${app.security.apiKey:}")
    private String apiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        // Protect non-GET requests
        if ("GET".equalsIgnoreCase(method)) {
            return true;
        }
        String header = request.getHeader("X-API-KEY");
        if (header == null || header.isEmpty() || apiKey == null || apiKey.isEmpty() || !apiKey.equals(header)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return false;
        }
        return true;
    }
}

