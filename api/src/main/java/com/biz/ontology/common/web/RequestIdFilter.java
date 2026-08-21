/**
 * 模块2：HTTP请求关联过滤器。
 * 功能：验证或生成请求ID，将其存储到MDC/上下文中，并在响应中回显。
 * 技术栈：Spring Web OncePerRequestFilter + SLF4J MDC。
 */
package com.biz.ontology.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String HEADER_NAME = "X-Request-Id";
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String supplied = request.getHeader(HEADER_NAME);
        String requestId = supplied != null && SAFE_REQUEST_ID.matcher(supplied).matches()
                ? supplied
                : UUID.randomUUID().toString();
        try {
            RequestIdContext.set(requestId);
            MDC.put("requestId", requestId);
            response.setHeader(HEADER_NAME, requestId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
            RequestIdContext.clear();
        }
    }
}
