-- 模块15：平台统一认证、授权和关键业务操作审计日志。
-- 功能：只追加记录主体、动作、结果、耗时和requestId，不存请求体、凭证、SQL或完整业务数据。
-- 技术栈：Flyway SQL迁移，兼容H2 MySQL模式和MySQL 8。

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(64) NOT NULL,
    principal_type VARCHAR(20) NOT NULL,
    principal_id VARCHAR(100) NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NULL,
    resource_id VARCHAR(100) NULL,
    result VARCHAR(20) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    path VARCHAR(500) NOT NULL,
    duration_ms BIGINT NOT NULL,
    client_ip VARCHAR(64) NULL,
    error_code VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_request ON audit_log (request_id);
CREATE INDEX idx_audit_principal ON audit_log (principal_type, principal_id);
CREATE INDEX idx_audit_action ON audit_log (action, result);
CREATE INDEX idx_audit_created ON audit_log (created_at);
