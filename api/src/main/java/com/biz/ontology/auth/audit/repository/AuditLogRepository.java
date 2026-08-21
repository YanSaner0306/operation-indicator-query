/**
 * 模块15：审计日志仓储。
 * 功能：支持只追加持久化和多条件分页检索，不提供更新或删除业务接口。
 * 技术栈：Spring Data JPA与Specification。
 */
package com.biz.ontology.auth.audit.repository;
import com.biz.ontology.auth.audit.model.AuditLogEntity;import org.springframework.data.jpa.repository.*;
public interface AuditLogRepository extends JpaRepository<AuditLogEntity,Long>,JpaSpecificationExecutor<AuditLogEntity>{}
