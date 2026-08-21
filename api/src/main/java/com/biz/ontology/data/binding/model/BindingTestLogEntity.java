/**
 * 模块11：Binding测试日志实体。
 * 功能：只记录测试状态、耗时、稳定错误码和requestId，不保存SQL或完整业务记录。
 * 技术栈：Spring Data JPA与只追加审计记录。
 */
package com.biz.ontology.data.binding.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="binding_test_log")
public class BindingTestLogEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="binding_id",nullable=false) private Long bindingId;
    @Column(nullable=false) private boolean success;
    @Column(name="duration_ms",nullable=false) private long durationMs;
    @Column(name="error_code",length=100) private String errorCode;
    @Column(nullable=false,length=500) private String message;
    @Column(name="request_id",length=64) private String requestId;
    @Column(name="tested_at",nullable=false) private LocalDateTime testedAt;
    public void setBindingId(Long v){bindingId=v;} public void setSuccess(boolean v){success=v;} public void setDurationMs(long v){durationMs=v;}
    public void setErrorCode(String v){errorCode=v;} public void setMessage(String v){message=v;} public void setRequestId(String v){requestId=v;} public void setTestedAt(LocalDateTime v){testedAt=v;}
}
