/**
 * 模块3：用户-角色分配网关。
 * 功能：以原子方式替换分配，并为用户响应/授权解析角色ID。
 * 技术栈：Spring Data JPA，使用显式JPQL投影和修改查询。
 */
package com.biz.ontology.auth.identity.repository;

import com.biz.ontology.auth.identity.model.AuthUserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AuthUserRoleRepository extends JpaRepository<AuthUserRoleEntity, Long> {
    @Query("select relation.roleId from AuthUserRoleEntity relation where relation.userId = :userId")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    @Query("select relation.userId from AuthUserRoleEntity relation where relation.roleId in :roleIds")
    List<Long> findUserIdsByRoleIds(@Param("roleIds") Collection<Long> roleIds);

    long countByRoleId(Long roleId);

    @Modifying
    void deleteByUserId(Long userId);
}
