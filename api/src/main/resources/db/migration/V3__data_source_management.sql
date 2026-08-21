-- 模块6-8：数据源配置、连接测试日志和元数据访问持久化结构。
-- 功能：保存加密后的连接凭据、启停状态、测试结果，严禁落库存储明文密码。
-- 技术栈：Flyway SQL迁移，兼容H2 MySQL模式和MySQL 8。

CREATE TABLE IF NOT EXISTS data_source_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    db_type VARCHAR(20) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    database_name VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_cipher TEXT NOT NULL,
    password_iv VARCHAR(64) NOT NULL,
    key_version VARCHAR(32) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_test_status VARCHAR(20) NULL,
    last_test_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uk_data_source_name ON data_source_config (name, deleted_flag);
CREATE INDEX idx_data_source_status ON data_source_config (status, deleted_flag);

CREATE TABLE IF NOT EXISTS data_source_test_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_source_id BIGINT NOT NULL,
    success BOOLEAN NOT NULL,
    message VARCHAR(500) NOT NULL,
    latency_ms BIGINT NOT NULL,
    tested_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_test_log_data_source FOREIGN KEY (data_source_id) REFERENCES data_source_config (id)
);

CREATE INDEX idx_data_source_test_log ON data_source_test_log (data_source_id, tested_at);
