/**
 * 模块6：外部数据源配置持久化实体。
 * 功能：保存主机、库名、账号和AES-GCM密文，响应层不会暴露密码、IV或密文。
 * 技术栈：Spring Data JPA、乐观锁和审计时间字段。
 */
package com.biz.ontology.data.model;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.persistence.VersionedEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_source_config")
public class DataSourceConfigEntity extends VersionedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Enumerated(EnumType.STRING) @Column(name = "db_type", nullable = false, length = 20) private DatabaseType dbType;
    @Column(nullable = false, length = 255) private String host;
    @Column(nullable = false) private Integer port;
    @Column(name = "database_name", nullable = false, length = 100) private String databaseName;
    @Column(nullable = false, length = 100) private String username;
    @Column(name = "password_cipher", nullable = false, columnDefinition = "TEXT") private String passwordCipher;
    @Column(name = "password_iv", nullable = false, length = 64) private String passwordIv;
    @Column(name = "key_version", nullable = false, length = 32) private String keyVersion;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ConfigStatus status;
    @Enumerated(EnumType.STRING) @Column(name = "last_test_status", length = 20) private ConnectionTestStatus lastTestStatus;
    @Column(name = "last_test_at") private LocalDateTime lastTestAt;
    @Column(name = "deleted_flag", nullable = false) private boolean deletedFlag;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DatabaseType getDbType() { return dbType; }
    public void setDbType(DatabaseType dbType) { this.dbType = dbType; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordCipher() { return passwordCipher; }
    public void setPasswordCipher(String passwordCipher) { this.passwordCipher = passwordCipher; }
    public String getPasswordIv() { return passwordIv; }
    public void setPasswordIv(String passwordIv) { this.passwordIv = passwordIv; }
    public String getKeyVersion() { return keyVersion; }
    public void setKeyVersion(String keyVersion) { this.keyVersion = keyVersion; }
    public ConfigStatus getStatus() { return status; }
    public void setStatus(ConfigStatus status) { this.status = status; }
    public ConnectionTestStatus getLastTestStatus() { return lastTestStatus; }
    public void setLastTestStatus(ConnectionTestStatus lastTestStatus) { this.lastTestStatus = lastTestStatus; }
    public LocalDateTime getLastTestAt() { return lastTestAt; }
    public void setLastTestAt(LocalDateTime lastTestAt) { this.lastTestAt = lastTestAt; }
    public boolean isDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(boolean deletedFlag) { this.deletedFlag = deletedFlag; }
}
