/**
 * 模块3测试：以事务性API闭环测试的方式，演练角色、权限和用户管理。
 * 技术栈：Spring Boot Test + MockMvc + Spring Security Test + H2/Flyway + BCrypt。
 */
package com.biz.ontology.auth;

import com.biz.ontology.auth.identity.repository.AuthUserRepository;
import com.biz.ontology.auth.identity.service.UserPermissionQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev-h2")
@DirtiesContext
@WithMockUser(username = "admin", authorities = "AUTH_MANAGE")
class AuthRbacIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserPermissionQueryService userPermissionQueryService;

    @Test
    void shouldCreateRoleAndUserWithoutReturningPasswordHash() throws Exception {
        JsonNode role = postJson("/api/v1/auth/roles", Map.of(
                "code", "DATA_OPERATOR",
                "name", "数据管理员",
                "permissionCodes", List.of("DATASOURCE_VIEW", "BINDING_VIEW")
        ));
        long roleId = role.path("data").path("id").asLong();

        JsonNode user = postJson("/api/v1/auth/users", Map.of(
                "username", "data.user",
                "displayName", "数据人员",
                "password", "SafePassword123!",
                "roleIds", List.of(roleId)
        ));

        assertThat(user.path("data").has("passwordHash")).isFalse();
        assertThat(user.path("data").path("roleIds").get(0).asLong()).isEqualTo(roleId);
        assertThat(passwordEncoder.matches(
                "SafePassword123!",
                userRepository.findByIdAndDeletedFlagFalse(user.path("data").path("id").asLong()).orElseThrow().getPasswordHash()
        )).isTrue();
    }

    @Test
    void shouldRejectUnknownPermissionCodeAndDuplicateRoleCode() throws Exception {
        mockMvc.perform(post("/api/v1/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "INVALID_PERMISSION_ROLE",
                                "name", "无效权限角色",
                                "permissionCodes", List.of("NOT_A_PERMISSION")
                        ))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("AUTH_PERMISSION_CODE_INVALID"));

        postJson("/api/v1/auth/roles", Map.of(
                "code", "UNIQUE_ROLE",
                "name", "唯一角色",
                "permissionCodes", List.of()
        ));
        mockMvc.perform(post("/api/v1/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "UNIQUE_ROLE",
                                "name", "重复角色",
                                "permissionCodes", List.of()
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_CODE_EXISTS"));
    }

    @Test
    void disabledRoleShouldNotBeAssignableAndStaleVersionShouldBeRejected() throws Exception {
        JsonNode role = postJson("/api/v1/auth/roles", Map.of(
                "code", "TEMP_DISABLED_ROLE",
                "name", "临时角色",
                "permissionCodes", List.of("ONTOLOGY_VIEW")
        ));
        long roleId = role.path("data").path("id").asLong();
        long roleVersion = role.path("data").path("version").asLong();

        mockMvc.perform(patch("/api/v1/auth/roles/{id}/enabled", roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "DISABLED",
                                "version", roleVersion
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "disabled.role.user",
                                "displayName", "禁用角色用户",
                                "password", "SafePassword123!",
                                "roleIds", List.of(roleId)
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_PRINCIPAL_DISABLED"));

        mockMvc.perform(put("/api/v1/auth/roles/{id}", roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "并发旧版本",
                                "version", roleVersion
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));
    }

    @Test
    void effectivePermissionsShouldMergeMultipleRolesWithoutDuplicates() throws Exception {
        JsonNode roleA = postJson("/api/v1/auth/roles", Map.of(
                "code", "PERMISSION_ROLE_A",
                "name", "权限角色A",
                "permissionCodes", List.of("ONTOLOGY_VIEW", "RULE_VIEW")
        ));
        JsonNode roleB = postJson("/api/v1/auth/roles", Map.of(
                "code", "PERMISSION_ROLE_B",
                "name", "权限角色B",
                "permissionCodes", List.of("ONTOLOGY_VIEW", "AUTH_MANAGE")
        ));
        JsonNode user = postJson("/api/v1/auth/users", Map.of(
                "username", "permission.user",
                "displayName", "权限聚合用户",
                "password", "SafePassword123!",
                "roleIds", List.of(
                        roleA.path("data").path("id").asLong(),
                        roleB.path("data").path("id").asLong()
                )
        ));

        assertThat(userPermissionQueryService.findEffectivePermissions(user.path("data").path("id").asLong()))
                .containsExactlyInAnyOrder("ONTOLOGY_VIEW", "RULE_VIEW", "AUTH_MANAGE");
    }

    private JsonNode postJson(String path, Object body) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }
}
