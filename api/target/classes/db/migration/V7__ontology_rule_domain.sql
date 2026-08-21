-- 模块9-13：本体/领域/规则核心表（原由 Hibernate ddl-auto=update 在 H2 下自动维护）。
-- 功能：为 MySQL 环境补齐本体、领域、规则模式，使 dev profile 可用 ddl-auto=validate。
-- 技术栈：Flyway SQL迁移，MySQL 8 兼容。

CREATE TABLE IF NOT EXISTS biz_domain (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          parent_id BIGINT NULL,
                                          name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL,
    sort_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
    );
CREATE INDEX idx_biz_domain_parent_id ON biz_domain (parent_id);
CREATE INDEX idx_biz_domain_status ON biz_domain (status);
CREATE INDEX idx_biz_domain_sort_order ON biz_domain (sort_order);

CREATE TABLE IF NOT EXISTS ontology (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
    );
CREATE INDEX idx_ontology_status ON ontology (status);
CREATE INDEX idx_ontology_updated_at ON ontology (updated_at);

CREATE TABLE IF NOT EXISTS ontology_property (
                                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 ontology_id BIGINT NOT NULL,
                                                 name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    data_type VARCHAR(20) NOT NULL,
    length_value INT NULL,
    precision_value INT NULL,
    scale_value INT NULL,
    required_flag BOOLEAN NOT NULL,
    unique_flag BOOLEAN NOT NULL,
    default_value VARCHAR(500) NULL,
    description VARCHAR(500) NULL,
    sort_order INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_property_ontology_code UNIQUE (ontology_id, code)
    );
CREATE INDEX idx_property_ontology ON ontology_property (ontology_id);
CREATE INDEX idx_property_status ON ontology_property (status);

CREATE TABLE IF NOT EXISTS ontology_relation (
                                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 source_ontology_id BIGINT NOT NULL,
                                                 target_ontology_id BIGINT NOT NULL,
                                                 name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    cardinality VARCHAR(20) NOT NULL,
    source_property_id BIGINT NULL,
    target_property_id BIGINT NULL,
    description VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
    );
CREATE INDEX idx_relation_source_ontology ON ontology_relation (source_ontology_id);
CREATE INDEX idx_relation_target_ontology ON ontology_relation (target_ontology_id);
CREATE INDEX idx_relation_source_property ON ontology_relation (source_property_id);
CREATE INDEX idx_relation_target_property ON ontology_relation (target_property_id);

CREATE TABLE IF NOT EXISTS domain_ontology_rel (
                                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                   domain_id BIGINT NOT NULL,
                                                   ontology_id BIGINT NOT NULL,
                                                   created_at TIMESTAMP NOT NULL,
                                                   CONSTRAINT uk_domain_ontology UNIQUE (domain_id, ontology_id)
    );
CREATE INDEX idx_domain_ontology_domain ON domain_ontology_rel (domain_id);
CREATE INDEX idx_domain_ontology_ontology ON domain_ontology_rel (ontology_id);

CREATE TABLE IF NOT EXISTS rule_definition (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    ontology_id BIGINT NOT NULL,
    description VARCHAR(500) NULL,
    current_version_id BIGINT NULL,
    enabled_flag BOOLEAN NOT NULL,
    deleted_flag BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
    );
CREATE INDEX idx_rule_ontology ON rule_definition (ontology_id);
CREATE INDEX idx_rule_current_version ON rule_definition (current_version_id);
CREATE INDEX idx_rule_enabled_deleted ON rule_definition (enabled_flag, deleted_flag);

CREATE TABLE IF NOT EXISTS rule_version (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            rule_id BIGINT NOT NULL,
                                            version_no INT NOT NULL,
                                            change_note VARCHAR(500) NULL,
    created_by VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_rule_version_no UNIQUE (rule_id, version_no)
    );
CREATE INDEX idx_rule_version_rule ON rule_version (rule_id);

CREATE TABLE IF NOT EXISTS rule_condition (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              rule_version_id BIGINT NOT NULL UNIQUE,
                                              ontology_id BIGINT NOT NULL,
                                              property_id BIGINT NOT NULL,
                                              operator VARCHAR(30) NOT NULL,
    compare_value VARCHAR(1000) NULL,
    value_type VARCHAR(20) NOT NULL
    );
CREATE INDEX idx_rule_condition_property ON rule_condition (property_id);

CREATE TABLE IF NOT EXISTS rule_action (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           rule_version_id BIGINT NOT NULL UNIQUE,
                                           action_type VARCHAR(30) NOT NULL,
    result_code VARCHAR(100) NOT NULL,
    result_name VARCHAR(100) NOT NULL,
    message VARCHAR(1000) NULL
    );