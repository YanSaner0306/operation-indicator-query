/**
 * 模块3-4：后台用户持久化模型。
 * 功能：存储身份资料、密码哈希、登录锁定状态和Token版本。
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

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_user")
public class AuthUserEntity extends VersionedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AuthStatus status = AuthStatus.ENABLED;

    @Column(name = "failed_login_count", nullable = false)
    private Integer failedLoginCount = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at", nullable = false)
    private LocalDateTime passwordChangedAt;

    @Column(name = "token_version", nullable = false)
    private Long tokenVersion = 0L;

    @Column(name = "deleted_flag", nullable = false)
    private boolean deletedFlag;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public AuthStatus getStatus() { return status; }
    public void setStatus(AuthStatus status) { this.status = status; }
    public Integer getFailedLoginCount() { return failedLoginCount; }
    public void setFailedLoginCount(Integer failedLoginCount) { this.failedLoginCount = failedLoginCount; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
    public Long getTokenVersion() { return tokenVersion; }
    public void setTokenVersion(Long tokenVersion) { this.tokenVersion = tokenVersion; }
    public boolean isDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(boolean deletedFlag) { this.deletedFlag = deletedFlag; }
}
