/**
 * 模块15：审计日志只读响应契约。
 * 功能：返回调用身份、动作、结果、耗时和错误码，不包含请求体或任何凭证内容。
 * 技术栈：Java 17 record与Jackson。
 */
package com.biz.ontology.api.auth.dto;
import com.biz.ontology.auth.audit.model.AuditResult;import java.time.LocalDateTime;
public record AuditLogResponse(Long id,String requestId,String principalType,String principalId,String action,String resourceType,String resourceId,AuditResult result,String httpMethod,String path,long durationMs,String clientIp,String errorCode,LocalDateTime createdAt){}
