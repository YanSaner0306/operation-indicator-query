-- 模块4：用户Token生命周期数据库迁移。
-- 功能：增加用户tokenVersion、Refresh Token轮换记录和Access Token吊销表。
-- 技术栈：Flyway SQL，兼容H2 MySQL模式和MySQL 8。

ALTER TABLE auth_user ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    jti VARCHAR(64) NOT NULL,
    family_id VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    rotated_to_id BIGINT NULL,
    client_ip VARCHAR(64) NULL,
    user_agent VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES auth_user (id)
);

CREATE INDEX idx_refresh_token_user ON refresh_token (user_id);
CREATE INDEX idx_refresh_token_family ON refresh_token (family_id);
CREATE INDEX idx_refresh_token_expires ON refresh_token (expires_at);

CREATE TABLE revoked_access_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    jti VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_revoked_access_token_user FOREIGN KEY (user_id) REFERENCES auth_user (id)
);

CREATE INDEX idx_revoked_access_expires ON revoked_access_token (expires_at);
