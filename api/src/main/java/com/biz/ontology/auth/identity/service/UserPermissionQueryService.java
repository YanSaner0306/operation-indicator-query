/**
 * 模块3：有效用户权限查询契约。
 * 功能：解析已启用的角色并去重其权限码，供未来登录/令牌模块使用。
 * 技术栈：Spring只读事务 + 显式Spring Data JPA连接仓库。
 */
package com.biz.ontology.auth.identity.service;

import com.biz.ontology.auth.identity.model.AuthPermissionEntity;
import com.biz.ontology.auth.identity.model.AuthRoleEntity;
import com.biz.ontology.auth.identity.model.AuthStatus;
import com.biz.ontology.auth.identity.repository.AuthPermissionRepository;
import com.biz.ontology.auth.identity.repository.AuthRolePermissionRepository;
import com.biz.ontology.auth.identity.repository.AuthRoleRepository;
import com.biz.ontology.auth.identity.repository.AuthUserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserPermissionQueryService {
    private final AuthUserRoleRepository userRoleRepository;
    private final AuthRoleRepository roleRepository;
    private final AuthRolePermissionRepository rolePermissionRepository;
    private final AuthPermissionRepository permissionRepository;

    public UserPermissionQueryService(
            AuthUserRoleRepository userRoleRepository,
            AuthRoleRepository roleRepository,
            AuthRolePermissionRepository rolePermissionRepository,
            AuthPermissionRepository permissionRepository
    ) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public Set<String> findEffectivePermissions(Long userId) {
        List<Long> assignedRoleIds = userRoleRepository.findRoleIdsByUserId(userId);
        Set<Long> enabledRoleIds = roleRepository.findAllById(assignedRoleIds).stream()
                .filter(role -> !role.isDeletedFlag() && role.getStatus() == AuthStatus.ENABLED)
                .map(AuthRoleEntity::getId)
                .collect(Collectors.toSet());
        if (enabledRoleIds.isEmpty()) {
            return Set.of();
        }
        Set<Long> permissionIds = new HashSet<>(rolePermissionRepository.findPermissionIdsByRoleIds(enabledRoleIds));
        return permissionRepository.findAllById(permissionIds).stream()
                .filter(permission -> !permission.isDeletedFlag())
                .map(AuthPermissionEntity::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }
}
