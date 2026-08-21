-- 模块14：API客户端、直接权限和API Key凭证生命周期。
-- 功能：保存机器身份、权限关联和只存摘要的可轮换凭证，明文secret永不落库。
-- 技术栈：Flyway SQL迁移，兼容H2 MySQL模式和MySQL 8。

CREATE TABLE IF NOT EXISTS api_client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_api_client_status ON api_client (status, deleted_flag);

CREATE TABLE IF NOT EXISTS api_client_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_client_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT fk_client_permission_client FOREIGN KEY (api_client_id) REFERENCES api_client (id),
    CONSTRAINT fk_client_permission_permission FOREIGN KEY (permission_id) REFERENCES auth_permission (id),
    UNIQUE (api_client_id, permission_id)
);

CREATE TABLE IF NOT EXISTS api_key_credential (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_client_id BIGINT NOT NULL,
    key_id VARCHAR(64) NOT NULL UNIQUE,
    key_prefix VARCHAR(20) NOT NULL,
    secret_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NULL,
    last_used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    CONSTRAINT fk_api_key_client FOREIGN KEY (api_client_id) REFERENCES api_client (id)
);

CREATE INDEX idx_api_key_client_status ON api_key_credential (api_client_id, status);
CREATE INDEX idx_api_key_expiry ON api_key_credential (expires_at, status);
