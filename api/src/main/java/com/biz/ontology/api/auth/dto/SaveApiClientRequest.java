/**
 * 模块14：API客户端创建和更新请求契约。
 * 功能：校验公开clientId、名称、直接权限集合和更新版本。
 * 技术栈：Java 17 record与Jakarta Bean Validation。
 */
package com.biz.ontology.api.auth.dto;
import jakarta.validation.constraints.*;
import java.util.Set;
public record SaveApiClientRequest(@NotBlank@Pattern(regexp="[a-z][a-z0-9_.-]{2,99}")String clientId,@NotBlank@Size(max=100)String name,Set<String> permissionCodes,Long version){public SaveApiClientRequest{permissionCodes=permissionCodes==null?Set.of():Set.copyOf(permissionCodes);}}
