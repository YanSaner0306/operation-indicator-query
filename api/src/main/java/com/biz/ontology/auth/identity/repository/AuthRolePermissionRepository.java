/**
 * 模块3：角色-权限分配网关。
 * 功能：替换角色权限，并为RBAC决策解析有效的权限ID。
 * 技术栈：Spring Data JPA，使用显式JPQL投影和修改查询。
 */
package com.biz.ontology.auth.identity.repository;

import com.biz.ontology.auth.identity.model.AuthRolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AuthRolePermissionRepository extends JpaRepository<AuthRolePermissionEntity, Long> {
    @Query("select relation.permissionId from AuthRolePermissionEntity relation where relation.roleId = :roleId")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);

    @Query("select relation.permissionId from AuthRolePermissionEntity relation where relation.roleId in :roleIds")
    List<Long> findPermissionIdsByRoleIds(@Param("roleIds") Collection<Long> roleIds);

    @Modifying
    void deleteByRoleId(Long roleId);
}
