-- 模块9-13：本体与外部数据表Binding、字段映射、筛选条件和测试日志。
-- 功能：在同一事务中保存单表Binding配置，并为即时查询和测试结果提供可追溯结构。
-- 技术栈：Flyway SQL迁移，兼容H2 MySQL模式和MySQL 8。

CREATE TABLE IF NOT EXISTS ontology_table_binding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    data_source_id BIGINT NOT NULL,
    schema_name VARCHAR(128) NULL,
    table_name VARCHAR(128) NOT NULL,
    ontology_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_test_status VARCHAR(20) NULL,
    last_test_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    deleted_flag BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_binding_data_source FOREIGN KEY (data_source_id) REFERENCES data_source_config (id)
);

CREATE UNIQUE INDEX uk_binding_name ON ontology_table_binding (name, deleted_flag);
CREATE INDEX idx_binding_source ON ontology_table_binding (data_source_id, status, deleted_flag);
CREATE INDEX idx_binding_ontology ON ontology_table_binding (ontology_id, status, deleted_flag);

CREATE TABLE IF NOT EXISTS ontology_field_binding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    binding_id BIGINT NOT NULL,
    source_column VARCHAR(128) NOT NULL,
    source_data_type VARCHAR(100) NOT NULL,
    ontology_property_id BIGINT NOT NULL,
    unique_key BOOLEAN NOT NULL DEFAULT FALSE,
    sequence_no INT NOT NULL,
    CONSTRAINT fk_field_binding_parent FOREIGN KEY (binding_id) REFERENCES ontology_table_binding (id),
    UNIQUE (binding_id, source_column),
    UNIQUE (binding_id, ontology_property_id)
);

CREATE INDEX idx_field_binding_property ON ontology_field_binding (ontology_property_id, binding_id);

CREATE TABLE IF NOT EXISTS binding_filter_condition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    binding_id BIGINT NOT NULL,
    source_column VARCHAR(128) NOT NULL,
    source_data_type VARCHAR(100) NOT NULL,
    operator VARCHAR(20) NOT NULL,
    typed_value VARCHAR(2000) NULL,
    sequence_no INT NOT NULL,
    CONSTRAINT fk_filter_binding_parent FOREIGN KEY (binding_id) REFERENCES ontology_table_binding (id)
);

CREATE INDEX idx_filter_binding_parent ON binding_filter_condition (binding_id, sequence_no);

CREATE TABLE IF NOT EXISTS binding_test_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    binding_id BIGINT NOT NULL,
    success BOOLEAN NOT NULL,
    duration_ms BIGINT NOT NULL,
    error_code VARCHAR(100) NULL,
    message VARCHAR(500) NOT NULL,
    request_id VARCHAR(64) NULL,
    tested_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_binding_test_parent FOREIGN KEY (binding_id) REFERENCES ontology_table_binding (id)
);

CREATE INDEX idx_binding_test_log ON binding_test_log (binding_id, tested_at);
