/**
 * 模块14：API Key创建或轮换请求契约。
 * 功能：允许指定到期时间，并可选择在新Key创建后吊销现有活动Key。
 * 技术栈：Java 17 record与时间序列化。
 */
package com.biz.ontology.api.auth.dto;
import java.time.LocalDateTime;
public record CreateApiKeyRequest(LocalDateTime expiresAt,Boolean revokeExisting){public boolean shouldRevokeExisting(){return revokeExisting==null||revokeExisting;}}
