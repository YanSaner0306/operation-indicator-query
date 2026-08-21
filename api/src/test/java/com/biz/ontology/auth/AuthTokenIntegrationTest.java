/**
 * 模块4-5测试：真实演练登录、Bearer访问、刷新轮换、重放检测、注销吊销和失败锁定。
 * 技术栈：Spring Boot Test、MockMvc、H2/Flyway、BCrypt、JJWT与HttpOnly Cookie。
 */
package com.biz.ontology.auth;

import com.biz.ontology.auth.identity.model.*;
import com.biz.ontology.auth.identity.repository.AuthUserRepository;
import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import java.time.LocalDateTime;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("dev-h2") @DirtiesContext
class AuthTokenIntegrationTest {
    @Autowired MockMvc mockMvc; @Autowired ObjectMapper objectMapper; @Autowired AuthUserRepository users; @Autowired PasswordEncoder encoder;

    @BeforeEach void createUser() {
        AuthUserEntity user = users.findByUsernameAndDeletedFlagFalse("token.user").orElseGet(AuthUserEntity::new);
        user.setUsername("token.user"); user.setDisplayName("令牌用户"); user.setPasswordHash(encoder.encode("SafePassword123!"));
        user.setPasswordChangedAt(LocalDateTime.now()); user.setStatus(AuthStatus.ENABLED); user.setFailedLoginCount(0); user.setLockedUntil(null);
        users.saveAndFlush(user);
    }

    @Test void shouldRotateRefreshDetectReplayAndRevokeAccessOnLogout() throws Exception {
        MvcResult login = login("SafePassword123!", status().isOk());
        JsonNode loginJson = objectMapper.readTree(login.getResponse().getContentAsByteArray());
        String access = loginJson.path("data").path("accessToken").asText();
        Cookie firstRefresh = login.getResponse().getCookie("refresh_token");
        assertThat(firstRefresh).isNotNull(); assertThat(firstRefresh.isHttpOnly()).isTrue();

        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + access))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.username").value("token.user"));

        MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/refresh").cookie(firstRefresh).header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk()).andReturn();
        Cookie secondRefresh = refreshed.getResponse().getCookie("refresh_token");
        String secondAccess = objectMapper.readTree(refreshed.getResponse().getContentAsByteArray()).path("data").path("accessToken").asText();
        assertThat(secondRefresh.getValue()).isNotEqualTo(firstRefresh.getValue());

        mockMvc.perform(post("/api/v1/auth/logout").cookie(secondRefresh).header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondAccess))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + secondAccess))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(firstRefresh).header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_REFRESH_REPLAYED"));
    }

    @Test void shouldLockAccountAfterFiveFailures() throws Exception {
        for (int index=0; index<5; index++) login("wrong-password", status().isUnauthorized());
        AuthUserEntity locked = users.findByUsernameAndDeletedFlagFalse("token.user").orElseThrow();
        assertThat(locked.getFailedLoginCount()).isEqualTo(5); assertThat(locked.getLockedUntil()).isAfter(LocalDateTime.now());
        login("SafePassword123!", status().isUnauthorized());
    }

    private MvcResult login(String password, ResultMatcher expected) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "token.user", "password", password))))
                .andExpect(expected).andReturn();
    }
}
