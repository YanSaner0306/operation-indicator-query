/**
 * 模块14-15测试：API 客户端认证、实时授权、凭证撤销与审计查询闭环。
 * 功能：验证完整 Key 仅创建时可得、Key 可访问授权接口、越权被拒、撤销立即失效，且调用记录可审计查询。
 * 技术栈：Spring Boot Test、MockMvc、Spring Security Test、H2/Flyway 与 Jackson。
 */
package com.biz.ontology.auth;

import com.biz.ontology.api.auth.dto.*;
import com.biz.ontology.auth.apiclient.service.ApiClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("dev-h2") @DirtiesContext
class ApiClientAuditIntegrationTest {
    @Autowired MockMvc mockMvc; @Autowired ApiClientService service; @Autowired ObjectMapper mapper;

    @Test void apiKeyLifecycleAndAuditShouldFormClosedLoop() throws Exception {
        ApiClientResponse client=service.create(new SaveApiClientRequest("binding_reader","绑定读取程序",Set.of("BINDING_VIEW"),null));
        ApiKeyCreatedResponse key=service.createCredential(client.id(),new CreateApiKeyRequest(null,true));
        assertThat(key.apiKey()).startsWith("ak_").contains(".");

        mockMvc.perform(get("/api/v1/bindings").header("X-API-Key",key.apiKey()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("SUCCESS"));
        mockMvc.perform(get("/api/v1/auth/audit-logs").header("X-API-Key",key.apiKey()))
                .andExpect(status().isForbidden());

        String page=mockMvc.perform(get("/api/v1/auth/api-clients").with(user("admin").authorities(()->"AUTH_MANAGE")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(page).doesNotContain(key.apiKey()).doesNotContain("secretHash").contains(key.keyPrefix());

        mockMvc.perform(get("/api/v1/auth/audit-logs").param("principalId","binding_reader").with(user("auditor").authorities(()->"AUDIT_VIEW")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items[0].principalType").value("API_CLIENT"));

        service.revoke(client.id(),key.keyId());
        mockMvc.perform(get("/api/v1/bindings").header("X-API-Key",key.apiKey()))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_API_KEY_REVOKED"));
    }
}
