/**
 * 模块6：数据源密码加密服务。
 * 功能：使用随机IV的AES-256-GCM加解密密码，并通过认证标签检测密文篡改或错误密钥。
 * 技术栈：Java Cryptography Architecture、AES/GCM/NoPadding与SecureRandom。
 */
package com.biz.ontology.data.security;

import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.data.config.DataSourceSecurityProperties;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordEncryptionService {
    private static final int IV_LENGTH = 12;
    private final DataSourceSecurityProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();
    public PasswordEncryptionService(DataSourceSecurityProperties properties) { this.properties = properties; }

    public EncryptedPassword encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptedPassword(Base64.getEncoder().encodeToString(ciphertext), Base64.getEncoder().encodeToString(iv), properties.getKeyVersion());
        } catch (Exception exception) {
            throw new IllegalStateException("数据源密码加密失败", exception);
        }
    }

    public String decrypt(String ciphertext, String iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, Base64.getDecoder().decode(iv)));
            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new BusinessException(PlatformErrorCode.DATASOURCE_CONFIG_INVALID, "数据源密码无法解密");
        }
    }

    private SecretKeySpec key() {
        try {
            byte[] bytes = Base64.getDecoder().decode(properties.getEncryptionKey());
            if (bytes.length != 32) throw new IllegalArgumentException();
            return new SecretKeySpec(bytes, "AES");
        } catch (Exception exception) {
            throw new IllegalStateException("BRRP_DATASOURCE_ENCRYPTION_KEY必须是32字节Base64密钥", exception);
        }
    }

    public record EncryptedPassword(String ciphertext, String iv, String keyVersion) {}
}
