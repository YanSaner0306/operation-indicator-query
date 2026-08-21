/**
 * 模块15：审计结果状态。
 * 功能：区分成功、业务失败和认证授权拒绝，便于查询与统计。
 * 技术栈：Java 17枚举与JPA字符串映射。
 */
package com.biz.ontology.auth.audit.model;
public enum AuditResult {SUCCESS,FAILED,DENIED}
