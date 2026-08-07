package com.dems.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility component extracting HTTP request metadata, client IP address, User-Agent, correlation ID,
 * and authenticated user context.
 */
public class AuditContextHolder {

    private AuditContextHolder() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static AuditContext currentContext() {
        String username = "ANONYMOUS";
        String role = "NONE";
        String ipAddress = "127.0.0.1";
        String userAgent = "SYSTEM";
        String correlationId = UUID.randomUUID().toString();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
            role = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = getClientIp(request);
            String agent = request.getHeader("User-Agent");
            if (agent != null && !agent.isBlank()) {
                userAgent = agent;
            }
            String correlationHeader = request.getHeader("X-Correlation-ID");
            if (correlationHeader != null && !correlationHeader.isBlank()) {
                correlationId = correlationHeader.trim();
            }
        }

        return AuditContext.builder()
                .username(username)
                .userRole(role)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .correlationId(correlationId)
                .build();
    }

    private static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
