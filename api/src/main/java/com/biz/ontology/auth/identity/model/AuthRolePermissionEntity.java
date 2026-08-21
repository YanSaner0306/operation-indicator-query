/**
 * 模块3：显式的角色-权限关联。
 * 功能：强制每个角色仅有一个权限分配，并保持RBAC连接可独立查询。
 * 技术栈：Jakarta Persistence/JPA连接实体，带有数据库唯一约束。
 */
package com.biz.ontology.auth.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "auth_role_permission", uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"}))
public class AuthRolePermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    public Long getId() { return id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getPermissionId() { return permissionId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
}
