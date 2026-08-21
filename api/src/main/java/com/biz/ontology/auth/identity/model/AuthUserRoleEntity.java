/**
 * 模块3：显式的用户-角色关联。
 * 功能：强制每个用户/角色对仅有一个分配，不采用隐式的@ManyToMany行为。
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
@Table(name = "auth_user_role", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"}))
public class AuthUserRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
