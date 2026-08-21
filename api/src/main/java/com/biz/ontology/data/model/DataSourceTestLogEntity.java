/**
 * 模块7：数据源连接测试审计实体。
 * 功能：记录成功状态、耗时和脱敏后的诊断消息，支持后续排障追溯。
 * 技术栈：Spring Data JPA实体映射。
 */
package com.biz.ontology.data.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_source_test_log")
public class DataSourceTestLogEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "data_source_id", nullable = false) private Long dataSourceId;
    @Column(nullable = false) private boolean success;
    @Column(nullable = false, length = 500) private String message;
    @Column(name = "latency_ms", nullable = false) private long latencyMs;
    @Column(name = "tested_at", nullable = false) private LocalDateTime testedAt;
    public void setDataSourceId(Long value) { dataSourceId = value; }
    public void setSuccess(boolean value) { success = value; }
    public void setMessage(String value) { message = value; }
    public void setLatencyMs(long value) { latencyMs = value; }
    public void setTestedAt(LocalDateTime value) { testedAt = value; }
}
