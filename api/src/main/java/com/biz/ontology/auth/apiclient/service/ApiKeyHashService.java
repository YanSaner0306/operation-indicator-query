/**
 * 模块14：API Key secret摘要服务。
 * 功能：使用环境注入pepper执行HMAC-SHA-256，并以常量时间比较摘要防止时序泄露。
 * 技术栈：Java Mac、SecretKeySpec、Base64与MessageDigest.isEqual。
 */
package com.biz.ontology.auth.apiclient.service;
import com.biz.ontology.auth.token.SecurityTokenProperties;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
@Service
public class ApiKeyHashService {
 private final SecurityTokenProperties properties;public ApiKeyHashService(SecurityTokenProperties p){properties=p;}
 public String hash(String secret){try{byte[] key=Base64.getDecoder().decode(properties.getApiKeyPepper());if(key.length<32)throw new IllegalStateException();Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(key,"HmacSHA256"));return Base64.getEncoder().encodeToString(mac.doFinal(secret.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException("BRRP_API_KEY_PEPPER必须是至少32字节Base64密钥",e);}}
 public boolean matches(String secret,String expected){return MessageDigest.isEqual(hash(secret).getBytes(StandardCharsets.US_ASCII),expected.getBytes(StandardCharsets.US_ASCII));}
}
