/**
 * 模块2、4、5：平台HTTP认证与授权过滤链。
 * 功能：安装requestId、Bearer JWT、URL权限矩阵和默认拒绝策略。
 * 技术栈：Spring Security 6 SecurityFilterChain、无状态会话、方法安全和BCrypt。
 */
package com.biz.ontology.config;

import com.biz.ontology.common.security.RestAccessDeniedHandler;
import com.biz.ontology.common.security.RestAuthenticationEntryPoint;
import com.biz.ontology.common.security.BearerTokenAuthenticationFilter;
import com.biz.ontology.common.security.ApiKeyAuthenticationFilter;
import com.biz.ontology.common.security.RateLimitFilter;
import com.biz.ontology.common.security.AuditRequestFilter;
import com.biz.ontology.common.web.RequestIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            RequestIdFilter requestIdFilter,
            BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter,
            ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
            RateLimitFilter rateLimitFilter,
            AuditRequestFilter auditRequestFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> { })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/actuator/health",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/rules/*/test").hasAuthority("RULE_TEST")
                        .requestMatchers(HttpMethod.GET, "/api/v1/rules/**", "/api/v1/ontologies/*/rules").hasAuthority("RULE_VIEW")
                        .requestMatchers("/api/v1/rules/**").hasAuthority("RULE_MANAGE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/domains/**", "/api/v1/ontologies/**", "/api/v1/ontology-graph/**")
                        .hasAuthority("ONTOLOGY_VIEW")
                        .requestMatchers("/api/v1/domains/**", "/api/v1/ontologies/**", "/api/v1/ontology-graph/**")
                        .hasAuthority("ONTOLOGY_MANAGE")
                        .requestMatchers(HttpMethod.POST, "/api/v1/data-sources/*/tables/*/preview").hasAuthority("DATASOURCE_VIEW")
                        .requestMatchers(HttpMethod.GET, "/api/v1/data-sources/**").hasAuthority("DATASOURCE_VIEW")
                        .requestMatchers("/api/v1/data-sources/**").hasAuthority("DATASOURCE_MANAGE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/bindings/**").hasAuthority("BINDING_VIEW")
                        .requestMatchers("/api/v1/bindings/**").hasAuthority("BINDING_MANAGE")
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(auditRequestFilter, RequestIdFilter.class)
                .addFilterAfter(rateLimitFilter, AuditRequestFilter.class)
                .addFilterAfter(bearerTokenAuthenticationFilter, RateLimitFilter.class)
                .addFilterAfter(apiKeyAuthenticationFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}
