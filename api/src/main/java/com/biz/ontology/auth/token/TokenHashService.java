/**
 * 模块4：不透明令牌摘要服务。
 * 功能：使用SHA-256生成Refresh Token的固定长度数据库摘要。
 * 技术栈：Java MessageDigest与十六进制编码。
 */
package com.biz.ontology.auth.token;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class TokenHashService {
    public String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JDK缺少SHA-256实现", exception);
        }
    }
}
