/**
 * 模块5测试：验证匿名、只读权限和管理权限在数据源URL矩阵上的访问边界。
 * 技术栈：Spring Boot Test、MockMvc与Spring Security Test。
 */
package com.biz.ontology.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("dev-h2") @DirtiesContext
class AuthorizationMatrixIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test void anonymousRequestShouldReturnJson401() throws Exception {
        mockMvc.perform(get("/api/v1/data-sources"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_TOKEN_INVALID"));
    }

    @Test @WithMockUser(authorities="DATASOURCE_VIEW")
    void viewPermissionShouldReadButNotCreate() throws Exception {
        mockMvc.perform(get("/api/v1/data-sources")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/data-sources").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_PERMISSION_DENIED"));
    }
}
