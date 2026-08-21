/**
 * 模块3：后台用户应用服务。
 * 功能：管理用户，对密码进行哈希处理/重置，并以原子方式替换已验证的角色分配。
 * 技术栈：Spring事务 + Spring Data JPA + Spring Security PasswordEncoder。
 */
package com.biz.ontology.auth.identity.service;

import com.biz.ontology.api.auth.dto.CreateUserRequest;
import com.biz.ontology.api.auth.dto.ResetPasswordRequest;
import com.biz.ontology.api.auth.dto.UpdateAuthStatusRequest;
import com.biz.ontology.api.auth.dto.UpdateUserRequest;
import com.biz.ontology.api.auth.dto.UserResponse;
import com.biz.ontology.api.common.PageResponse;
import com.biz.ontology.auth.identity.model.AuthRoleEntity;
import com.biz.ontology.auth.identity.model.AuthStatus;
import com.biz.ontology.auth.identity.model.AuthUserEntity;
import com.biz.ontology.auth.identity.model.AuthUserRoleEntity;
import com.biz.ontology.auth.identity.repository.AuthUserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Service
public class UserService {
    private final AuthUserRepository userRepository;
    private final AuthUserRoleRepository userRoleRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public UserService(
            AuthUserRepository userRepository,
            AuthUserRoleRepository userRoleRepository,
            RoleService roleService,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> page(String keyword, AuthStatus status, int page, int size) {
        Specification<AuthUserEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isFalse(root.get("deletedFlag")));
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("username")), pattern),
                        builder.like(builder.lower(root.get("displayName")), pattern)
                ));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        Page<AuthUserEntity> result = userRepository.findAll(
                specification,
                PageRequest.of(page - 1, size, Sort.by("username").ascending())
        );
        return new PageResponse<>(result.getContent().stream().map(this::toResponse).toList(), page, size, result.getTotalElements());
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim().toLowerCase();
        if (userRepository.existsByUsernameAndDeletedFlagFalse(username)) {
            throw new BusinessException(PlatformErrorCode.AUTH_CODE_EXISTS);
        }
        List<AuthRoleEntity> roles = roleService.requireEnabledRoles(request.roleIds());
        AuthUserEntity user = new AuthUserEntity();
        user.setUsername(username);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setStatus(AuthStatus.ENABLED);
        AuthUserEntity savedUser = userRepository.saveAndFlush(user);
        replaceRoles(savedUser.getId(), roles);
        return toResponse(savedUser);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        AuthUserEntity user = requireUser(id);
        requireVersion(user.getVersion(), request.version());
        List<AuthRoleEntity> roles = roleService.requireEnabledRoles(request.roleIds());
        user.setDisplayName(request.displayName().trim());
        userRoleRepository.deleteByUserId(user.getId());
        userRoleRepository.flush();
        replaceRoles(user.getId(), roles);
        entityManager.lock(user, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        userRepository.flush();
        entityManager.refresh(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateStatus(Long id, UpdateAuthStatusRequest request) {
        AuthUserEntity user = requireUser(id);
        requireVersion(user.getVersion(), request.version());
        user.setStatus(request.status());
        userRepository.saveAndFlush(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse resetPassword(Long id, ResetPasswordRequest request) {
        AuthUserEntity user = requireUser(id);
        requireVersion(user.getVersion(), request.version());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userRepository.saveAndFlush(user);
        return toResponse(user);
    }

    private AuthUserEntity requireUser(Long id) {
        return userRepository.findByIdAndDeletedFlagFalse(id)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.AUTH_USER_NOT_FOUND));
    }

    private void replaceRoles(Long userId, List<AuthRoleEntity> roles) {
        List<AuthUserRoleEntity> relations = roles.stream().map(role -> {
            AuthUserRoleEntity relation = new AuthUserRoleEntity();
            relation.setUserId(userId);
            relation.setRoleId(role.getId());
            return relation;
        }).toList();
        userRoleRepository.saveAll(relations);
    }

    private UserResponse toResponse(AuthUserEntity user) {
        Set<Long> roleIds = new TreeSet<>(userRoleRepository.findRoleIdsByUserId(user.getId()));
        return new UserResponse(
                user.getId(), user.getUsername(), user.getDisplayName(), user.getStatus(), roleIds,
                user.getLastLoginAt(), user.getLockedUntil(), user.getVersion(), user.getCreatedAt(), user.getUpdatedAt()
        );
    }

    private void requireVersion(Long actual, Long requested) {
        if (!Objects.equals(actual, requested)) {
            throw new BusinessException(PlatformErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
    }
}
