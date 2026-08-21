/**
 * 模块2：JSON授权失败处理器。
 * 功能：返回HTTP 403状态，附带稳定的AUTH_PERMISSION_DENIED错误码和当前请求ID。
 * 技术栈：Spring Security AccessDeniedHandler + Jackson ObjectMapper。
 */
package com.biz.ontology.common.security;

import com.biz.ontology.api.common.R;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        PlatformErrorCode code = PlatformErrorCode.AUTH_PERMISSION_DENIED;
        response.setStatus(code.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), R.error(code.getResponseCode(), code.getDefaultMessage()));
    }
}
