/**
 * 模块6测试：验证AES-GCM密码加密的随机性、可逆性和篡改检测。
 * 技术栈：JUnit 5、AssertJ与Java Cryptography Architecture。
 */
package com.biz.ontology.data;

import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.data.config.DataSourceSecurityProperties;
import com.biz.ontology.data.security.PasswordEncryptionService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PasswordEncryptionServiceTest {
    @Test void shouldUseRandomIvAndRejectTamperedCiphertext() {
        DataSourceSecurityProperties properties = new DataSourceSecurityProperties();
        properties.setEncryptionKey("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        PasswordEncryptionService service = new PasswordEncryptionService(properties);
        var first = service.encrypt("database-secret"); var second = service.encrypt("database-secret");
        assertThat(first.ciphertext()).doesNotContain("database-secret").isNotEqualTo(second.ciphertext());
        assertThat(first.iv()).isNotEqualTo(second.iv());
        assertThat(service.decrypt(first.ciphertext(), first.iv())).isEqualTo("database-secret");
        assertThatThrownBy(() -> service.decrypt(first.ciphertext() + "A", first.iv())).isInstanceOf(BusinessException.class);
    }
}
