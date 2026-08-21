/**
 * 模块2：JSON认证失败处理器。
 * 功能：返回HTTP 401状态，附带稳定的AUTH_TOKEN_INVALID错误码和当前请求ID。
 * 技术栈：Spring Security AuthenticationEntryPoint + Jackson ObjectMapper。
 */
package com.biz.ontology.common.security;

import com.biz.ontology.api.common.R;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        PlatformErrorCode code = PlatformErrorCode.AUTH_TOKEN_INVALID;
        response.setStatus(code.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), R.error(code.getResponseCode(), code.getDefaultMessage()));
    }
}
