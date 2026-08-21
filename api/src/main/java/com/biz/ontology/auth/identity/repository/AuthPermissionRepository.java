/**
 * 模块3：权限字典持久化网关。
 * 功能：列出后端拥有的字典，并解析提交的权限码。
 * 技术栈：Spring Data JPA派生查询。
 */
package com.biz.ontology.auth.identity.repository;

import com.biz.ontology.auth.identity.model.AuthPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AuthPermissionRepository extends JpaRepository<AuthPermissionEntity, Long> {
    List<AuthPermissionEntity> findAllByDeletedFlagFalseOrderByModuleAscCodeAsc();
    List<AuthPermissionEntity> findAllByCodeInAndDeletedFlagFalse(Collection<String> codes);
}
