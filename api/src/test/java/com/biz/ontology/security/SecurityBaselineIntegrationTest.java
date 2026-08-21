/**
 * 模块2测试：验证默认身份验证、403授权和请求ID关联。
 * 技术栈：Spring Boot Test + MockMvc + Spring Security Test，基于H2/Flyway。
 */
package com.biz.ontology.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev-h2")
class SecurityBaselineIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousBusinessRequestShouldReturn401WithGeneratedRequestId() throws Exception {
        mockMvc.perform(get("/api/v1/data/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_TOKEN_INVALID"))
                .andExpect(jsonPath("$.requestId").value(matchesPattern("[A-Za-z0-9-]{36}")))
                .andExpect(header().string("X-Request-Id", matchesPattern("[A-Za-z0-9-]{36}")));
    }

    @Test
    void safeCallerRequestIdShouldBeEchoed() throws Exception {
        mockMvc.perform(get("/api/v1/data/ping").header("X-Request-Id", "req-client-123"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Request-Id", "req-client-123"))
                .andExpect(jsonPath("$.requestId").value("req-client-123"));
    }

    @Test
    @WithMockUser(authorities = "ONTOLOGY_VIEW")
    void authenticatedUserWithoutRequiredPermissionShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/auth/permissions"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_PERMISSION_DENIED"));
    }

    @Test
    @WithMockUser(authorities = "AUTH_MANAGE")
    void authenticatedManagerShouldReadPermissionDictionary() throws Exception {
        mockMvc.perform(get("/api/v1/auth/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[?(@.code == 'AUTH_MANAGE')]").exists())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }
}
