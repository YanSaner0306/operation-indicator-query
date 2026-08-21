/**
 * 模块14：API客户端直接权限关联实体。
 * 功能：让机器身份复用平台权限字典，但不引入角色或资源级ACL。
 * 技术栈：Spring Data JPA关联表映射。
 */
package com.biz.ontology.auth.apiclient.model;

import jakarta.persistence.*;

@Entity @Table(name="api_client_permission")
public class ApiClientPermissionEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="api_client_id",nullable=false) private Long apiClientId;
    @Column(name="permission_id",nullable=false) private Long permissionId;
    public void setApiClientId(Long v){apiClientId=v;} public void setPermissionId(Long v){permissionId=v;}
}
