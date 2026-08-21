/**
 * 模块3：用户持久化网关。
 * 功能：支持活动记录查找、唯一性检查以及基于规范的分页。
 * 技术栈：Spring Data JPA仓库和JpaSpecificationExecutor。
 */
package com.biz.ontology.auth.identity.repository;

import com.biz.ontology.auth.identity.model.AuthUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, Long>, JpaSpecificationExecutor<AuthUserEntity> {
    Optional<AuthUserEntity> findByIdAndDeletedFlagFalse(Long id);
    Optional<AuthUserEntity> findByUsernameAndDeletedFlagFalse(String username);
    boolean existsByUsernameAndDeletedFlagFalse(String username);
}
