/**
 * 模块3：后端拥有的权限字典条目。
 * 功能：定义角色和未来API客户端可能接收的有效权限码。
 * 技术栈：Jakarta Persistence/JPA，由Flyway种子数据提供支持。
 */
package com.biz.ontology.auth.identity.model;

import com.biz.ontology.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_permission")
public class AuthPermissionEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "deleted_flag", nullable = false)
    private boolean deletedFlag;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public boolean isDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(boolean deletedFlag) { this.deletedFlag = deletedFlag; }
}
