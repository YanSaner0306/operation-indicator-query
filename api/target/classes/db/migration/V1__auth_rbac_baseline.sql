-- 模块1：平台数据库的初始认证/RBAC持久化基线。
-- 功能：创建用户、角色、权限及其显式的关联表。
-- 技术栈：Flyway SQL迁移，可跨H2 MySQL模式及MySQL 8移植。

CREATE TABLE IF NOT EXISTS auth_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failed_login_count INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    password_changed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE (user_id, role_id),
    CONSTRAINT fk_auth_user_role_user FOREIGN KEY (user_id) REFERENCES auth_user (id),
    CONSTRAINT fk_auth_user_role_role FOREIGN KEY (role_id) REFERENCES auth_role (id)
);

CREATE TABLE IF NOT EXISTS auth_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE (role_id, permission_id),
    CONSTRAINT fk_auth_role_permission_role FOREIGN KEY (role_id) REFERENCES auth_role (id),
    CONSTRAINT fk_auth_role_permission_permission FOREIGN KEY (permission_id) REFERENCES auth_permission (id)
);

CREATE INDEX idx_auth_user_status ON auth_user (status);
CREATE INDEX idx_auth_role_status ON auth_role (status);
CREATE INDEX idx_auth_permission_module ON auth_permission (module);

INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'ONTOLOGY_VIEW', '查看本体', 'ONTOLOGY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'ONTOLOGY_VIEW');
INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'ONTOLOGY_MANAGE', '管理本体', 'ONTOLOGY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'ONTOLOGY_MANAGE');
INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'DATASOURCE_VIEW', '查看数据源', 'DATA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'DATASOURCE_VIEW');
INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'DATASOURCE_MANAGE', '管理数据源', 'DATA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'DATASOURCE_MANAGE');
INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'BINDING_VIEW', '查看Binding', 'DATA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'BINDING_VIEW');
INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'BINDING_MANAGE', '管理Binding', 'DATA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'BINDING_MANAGE');
INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'RULE_VIEW', '查看规则', 'RULE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'RULE_VIEW');
INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'RULE_MANAGE', '管理规则', 'RULE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'RULE_MANAGE');
INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'RULE_TEST', '测试规则', 'RULE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'RULE_TEST');
INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'AUTH_MANAGE', '管理鉴权', 'AUTH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'AUTH_MANAGE');
INSERT INTO auth_permission (code, name, module, created_at, updated_at, deleted_flag)
SELECT 'AUDIT_VIEW', '查看审计', 'AUTH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE
WHERE NOT EXISTS (SELECT 1 FROM auth_permission WHERE code = 'AUDIT_VIEW');
