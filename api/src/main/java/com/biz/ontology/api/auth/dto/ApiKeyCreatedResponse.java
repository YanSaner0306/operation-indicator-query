/**
 * 模块14：API Key一次性明文响应。
 * 功能：仅在创建或轮换成功的当前响应返回完整Key，后续接口只返回keyPrefix。
 * 技术栈：Java 17 record与Jackson序列化。
 */
package com.biz.ontology.api.auth.dto;
import java.time.LocalDateTime;
public record ApiKeyCreatedResponse(String clientId,String keyId,String keyPrefix,String apiKey,LocalDateTime expiresAt){}
