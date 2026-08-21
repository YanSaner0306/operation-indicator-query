/**
 * 模块14：API客户端机器身份实体。
 * 功能：保存公开clientId、显示名称、启停状态和最近调用时间，不保存任何明文凭证。
 * 技术栈：Spring Data JPA、乐观锁、软删除和审计字段。
 */
package com.biz.ontology.auth.apiclient.model;

import com.biz.ontology.auth.identity.model.AuthStatus;
import com.biz.ontology.common.persistence.VersionedEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="api_client")
public class ApiClientEntity extends VersionedEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="client_id",nullable=false,unique=true,length=100) private String clientId;
    @Column(nullable=false,length=100) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private AuthStatus status;
    @Column(name="last_used_at") private LocalDateTime lastUsedAt;
    @Column(name="deleted_flag",nullable=false) private boolean deletedFlag;
    public Long getId(){return id;} public String getClientId(){return clientId;} public void setClientId(String v){clientId=v;}
    public String getName(){return name;} public void setName(String v){name=v;} public AuthStatus getStatus(){return status;} public void setStatus(AuthStatus v){status=v;}
    public LocalDateTime getLastUsedAt(){return lastUsedAt;} public void setLastUsedAt(LocalDateTime v){lastUsedAt=v;} public boolean isDeletedFlag(){return deletedFlag;} public void setDeletedFlag(boolean v){deletedFlag=v;}
}
