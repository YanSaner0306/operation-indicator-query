/**
 * 模块2：管理前端显式的浏览器来源策略。
 * 功能：允许配置的来源和REST方法，且不将通配符来源与凭据结合。
 * 技术栈：由外部化属性驱动的Spring MVC CORS配置。
 */
package com.biz.ontology.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String[] allowedOrigins;

    public CorsConfig(@Value("${platform.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins.split(",");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Request-Id", "X-API-Key")
                .exposedHeaders("X-Request-Id")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
