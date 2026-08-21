/**
 * 模块3：RBAC角色持久化模型。
 * 功能：存储稳定的角色编码、显示名称、状态和逻辑删除状态。
 * 技术栈：Jakarta Persistence/JPA，并从VersionedEntity继承乐观锁。
 */
package com.biz.ontology.auth.identity.model;

import com.biz.ontology.common.persistence.VersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_role")
public class AuthRoleEntity extends VersionedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AuthStatus status = AuthStatus.ENABLED;

    @Column(name = "deleted_flag", nullable = false)
    private boolean deletedFlag;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AuthStatus getStatus() { return status; }
    public void setStatus(AuthStatus status) { this.status = status; }
    public boolean isDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(boolean deletedFlag) { this.deletedFlag = deletedFlag; }
}
