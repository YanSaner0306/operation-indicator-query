/**
 * 模块3：RBAC角色应用服务。
 * 功能：管理角色生命周期，并以原子方式替换已验证的权限分配。
 * 技术栈：Spring事务 + Spring Data JPA + 乐观锁。
 */
package com.biz.ontology.auth.identity.service;

import com.biz.ontology.api.auth.dto.CreateRoleRequest;
import com.biz.ontology.api.auth.dto.RoleResponse;
import com.biz.ontology.api.auth.dto.SetRolePermissionsRequest;
import com.biz.ontology.api.auth.dto.UpdateAuthStatusRequest;
import com.biz.ontology.api.auth.dto.UpdateRoleRequest;
import com.biz.ontology.api.common.PageResponse;
import com.biz.ontology.auth.identity.model.AuthPermissionEntity;
import com.biz.ontology.auth.identity.model.AuthRoleEntity;
import com.biz.ontology.auth.identity.model.AuthRolePermissionEntity;
import com.biz.ontology.auth.identity.model.AuthStatus;
import com.biz.ontology.auth.identity.repository.AuthPermissionRepository;
import com.biz.ontology.auth.identity.repository.AuthRolePermissionRepository;
import com.biz.ontology.auth.identity.repository.AuthRoleRepository;
import com.biz.ontology.auth.identity.repository.AuthUserRoleRepository;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Service
public class RoleService {
    private final AuthRoleRepository roleRepository;
    private final AuthPermissionRepository permissionRepository;
    private final AuthRolePermissionRepository rolePermissionRepository;
    private final AuthUserRoleRepository userRoleRepository;
    private final PermissionService permissionService;
    private final EntityManager entityManager;

    public RoleService(
            AuthRoleRepository roleRepository,
            AuthPermissionRepository permissionRepository,
            AuthRolePermissionRepository rolePermissionRepository,
            AuthUserRoleRepository userRoleRepository,
            PermissionService permissionService,
            EntityManager entityManager
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.permissionService = permissionService;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> page(String keyword, AuthStatus status, int page, int size) {
        Specification<AuthRoleEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isFalse(root.get("deletedFlag")));
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("code")), pattern),
                        builder.like(builder.lower(root.get("name")), pattern)
                ));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        Page<AuthRoleEntity> result = roleRepository.findAll(
                specification,
                PageRequest.of(page - 1, size, Sort.by("code").ascending())
        );
        return new PageResponse<>(result.getContent().stream().map(this::toResponse).toList(), page, size, result.getTotalElements());
    }

    @Transactional
    public RoleResponse create(CreateRoleRequest request) {
        String code = request.code().trim().toUpperCase();
        if (roleRepository.existsByCodeAndDeletedFlagFalse(code)) {
            throw new BusinessException(PlatformErrorCode.AUTH_CODE_EXISTS);
        }
        List<AuthPermissionEntity> permissions = permissionService.requireAll(request.permissionCodes());
        AuthRoleEntity role = new AuthRoleEntity();
        role.setCode(code);
        role.setName(request.name().trim());
        role.setStatus(AuthStatus.ENABLED);
        AuthRoleEntity savedRole = roleRepository.saveAndFlush(role);
        replacePermissions(savedRole.getId(), permissions);
        return toResponse(savedRole);
    }

    @Transactional
    public RoleResponse update(Long id, UpdateRoleRequest request) {
        AuthRoleEntity role = requireRole(id);
        requireVersion(role.getVersion(), request.version());
        role.setName(request.name().trim());
        entityManager.lock(role, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        roleRepository.flush();
        entityManager.refresh(role);
        return toResponse(role);
    }

    @Transactional
    public RoleResponse updateStatus(Long id, UpdateAuthStatusRequest request) {
        AuthRoleEntity role = requireRole(id);
        requireVersion(role.getVersion(), request.version());
        role.setStatus(request.status());
        roleRepository.saveAndFlush(role);
        return toResponse(role);
    }

    @Transactional
    public RoleResponse setPermissions(Long id, SetRolePermissionsRequest request) {
        AuthRoleEntity role = requireRole(id);
        requireVersion(role.getVersion(), request.version());
        List<AuthPermissionEntity> permissions = permissionService.requireAll(request.permissionCodes());
        rolePermissionRepository.deleteByRoleId(role.getId());
        rolePermissionRepository.flush();
        replacePermissions(role.getId(), permissions);
        entityManager.lock(role, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        roleRepository.flush();
        entityManager.refresh(role);
        return toResponse(role);
    }

    @Transactional(readOnly = true)
    public List<AuthRoleEntity> requireEnabledRoles(Set<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<AuthRoleEntity> roles = roleRepository.findAllByIdInAndDeletedFlagFalse(roleIds);
        Set<Long> foundIds = new HashSet<>(roles.stream().map(AuthRoleEntity::getId).toList());
        if (!foundIds.equals(roleIds)) {
            throw new BusinessException(PlatformErrorCode.AUTH_ROLE_NOT_FOUND);
        }
        if (roles.stream().anyMatch(role -> role.getStatus() != AuthStatus.ENABLED)) {
            throw new BusinessException(PlatformErrorCode.AUTH_PRINCIPAL_DISABLED, "不能分配已禁用角色");
        }
        return roles;
    }

    private AuthRoleEntity requireRole(Long id) {
        return roleRepository.findByIdAndDeletedFlagFalse(id)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.AUTH_ROLE_NOT_FOUND));
    }

    private void replacePermissions(Long roleId, List<AuthPermissionEntity> permissions) {
        List<AuthRolePermissionEntity> relations = permissions.stream().map(permission -> {
            AuthRolePermissionEntity relation = new AuthRolePermissionEntity();
            relation.setRoleId(roleId);
            relation.setPermissionId(permission.getId());
            return relation;
        }).toList();
        rolePermissionRepository.saveAll(relations);
    }

    private RoleResponse toResponse(AuthRoleEntity role) {
        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(role.getId());
        Set<String> permissionCodes = new TreeSet<>();
        permissionRepository.findAllById(permissionIds).forEach(permission -> permissionCodes.add(permission.getCode()));
        return new RoleResponse(
                role.getId(), role.getCode(), role.getName(), role.getStatus(), permissionCodes,
                userRoleRepository.countByRoleId(role.getId()), role.getVersion(), role.getCreatedAt(), role.getUpdatedAt()
        );
    }

    private void requireVersion(Long actual, Long requested) {
        if (!Objects.equals(actual, requested)) {
            throw new BusinessException(PlatformErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
    }
}
