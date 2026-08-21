/**
 * 模块3：角色持久化网关。
 * 功能：支持活动角色查找、编码唯一性检查以及基于规范的分页。
 * 技术栈：Spring Data JPA仓库和JpaSpecificationExecutor。
 */
package com.biz.ontology.auth.identity.repository;

import com.biz.ontology.auth.identity.model.AuthRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AuthRoleRepository extends JpaRepository<AuthRoleEntity, Long>, JpaSpecificationExecutor<AuthRoleEntity> {
    Optional<AuthRoleEntity> findByIdAndDeletedFlagFalse(Long id);
    Optional<AuthRoleEntity> findByCodeAndDeletedFlagFalse(String code);
    boolean existsByCodeAndDeletedFlagFalse(String code);
    List<AuthRoleEntity> findAllByIdInAndDeletedFlagFalse(Collection<Long> ids);
}
