/**
 * 模块14测试：API Key 摘要与校验。
 * 功能：验证 HMAC-SHA-256 结果稳定、正确密钥常量时间匹配、错误密钥拒绝以及短 pepper 拒绝启动使用。
 * 技术栈：JUnit 5、AssertJ、Java Base64 与 HMAC 服务单元测试。
 */
package com.biz.ontology.auth;

import com.biz.ontology.auth.apiclient.service.ApiKeyHashService;
import com.biz.ontology.auth.token.SecurityTokenProperties;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;

class ApiKeyHashServiceTest {
    @Test void shouldHashAndMatchWithoutStoringPlaintext(){var p=properties(new byte[32]);var service=new ApiKeyHashService(p);String hash=service.hash("secret-value");assertThat(hash).isNotEqualTo("secret-value");assertThat(service.hash("secret-value")).isEqualTo(hash);assertThat(service.matches("secret-value",hash)).isTrue();assertThat(service.matches("wrong",hash)).isFalse();}
    @Test void shouldRejectPepperShorterThanThirtyTwoBytes(){var service=new ApiKeyHashService(properties(new byte[8]));assertThatThrownBy(()->service.hash("secret")).isInstanceOf(IllegalStateException.class).hasMessageContaining("BRRP_API_KEY_PEPPER");}
    private SecurityTokenProperties properties(byte[] bytes){var p=new SecurityTokenProperties();p.setApiKeyPepper(Base64.getEncoder().encodeToString(bytes));return p;}
}
