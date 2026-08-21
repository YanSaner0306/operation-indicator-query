/**
 * 模块14：API客户端实时权限服务。
 * 功能：解析直接权限关联供管理响应和每次API Key认证加载，保证权限变更立即生效。
 * 技术栈：Spring事务与Spring Data JPA权限字典。
 */
package com.biz.ontology.auth.apiclient.service;
import com.biz.ontology.auth.apiclient.repository.ApiClientPermissionRepository;
import com.biz.ontology.auth.identity.repository.AuthPermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class ApiClientPermissionService {private final ApiClientPermissionRepository relations;private final AuthPermissionRepository permissions;public ApiClientPermissionService(ApiClientPermissionRepository a,AuthPermissionRepository b){relations=a;permissions=b;}@Transactional(readOnly=true)public Set<String> find(Long id){return permissions.findAllById(relations.findPermissionIds(id)).stream().filter(p->!p.isDeletedFlag()).map(p->p.getCode()).collect(Collectors.toUnmodifiableSet());}}
