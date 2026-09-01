package com.yu.blog.module.log.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class OperationLogFilter extends OncePerRequestFilter {
    private final OperationLogService operationLogService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            String[] route = routeParts(request);
            String action = actionFor(request, route);
            if (action != null) {
                operationLogService.record(request, moduleFor(request, route), action, idFor(route), response.getStatus());
            }
        }
    }

    private String actionFor(HttpServletRequest request, String[] route) {
        String path = request.getRequestURI();
        if ("/api/auth/login".equals(path) && "POST".equals(request.getMethod())) {
            return request.getAttribute("yu-log.operator-user-id") != null ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        }
        if ("/api/auth/change-password".equals(path) && "POST".equals(request.getMethod())) {
            return "PASSWORD_CHANGE";
        }
        if (route.length < 2 || !path.startsWith("/api/admin/") || "GET".equals(request.getMethod())) {
            return null;
        }
        String resource = route[0];
        String suffix = route.length > 2 ? route[2] : "";
        if ("status".equals(suffix)) {
            return switch (resource) {
                case "comments" -> "COMMENT_REVIEW";
                case "messages" -> "MESSAGE_REVIEW";
                case "articles" -> "ARTICLE_PUBLISH";
                default -> resource.toUpperCase() + "_STATUS";
            };
        }
        if ("reply".equals(suffix)) {
            return resource.equals("comments") ? "COMMENT_REPLY" : "MESSAGE_REPLY";
        }
        String operation = switch (request.getMethod()) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> null;
        };
        if (operation == null) {
            return null;
        }
        if ("site-settings".equals(resource)) {
            return "SITE_SETTING_UPDATE";
        }
        if ("files".equals(resource)) {
            return "FILE_UPLOAD";
        }
        return resource.toUpperCase().replace('-', '_') + "_" + operation;
    }

    private String moduleFor(HttpServletRequest request, String[] route) {
        if (request.getRequestURI().startsWith("/api/auth/")) {
            return "AUTH";
        }
        if (route.length == 0) {
            return "ADMIN";
        }
        return switch (route[0]) {
            case "articles" -> "ARTICLE";
            case "projects" -> "PROJECT";
            case "notes" -> "NOTE";
            case "timeline" -> "TIMELINE";
            case "comments" -> "COMMENT";
            case "messages" -> "MESSAGE";
            case "categories" -> "CATEGORY";
            case "tags" -> "TAG";
            case "site-settings" -> "SITE";
            case "files" -> "FILE";
            default -> "ADMIN";
        };
    }

    private Long idFor(String[] route) {
        for (String part : route) {
            if (part.matches("\\d+")) {
                try {
                    return Long.valueOf(part);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private String[] routeParts(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/")) {
            return Arrays.stream(path.substring("/api/admin/".length()).split("/"))
                    .filter(StringUtils::hasText)
                    .toArray(String[]::new);
        }
        if (path.startsWith("/api/auth/")) {
            return Arrays.stream(path.substring("/api/auth/".length()).split("/"))
                    .filter(StringUtils::hasText)
                    .toArray(String[]::new);
        }
        return new String[0];
    }
}
