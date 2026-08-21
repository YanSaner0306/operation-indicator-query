/**
 * Module 3: Permission dictionary application service.
 * Function: exposes backend-owned permission choices and rejects unknown submitted codes.
 * Stack: Spring transactional service + Spring Data JPA.
 */
package com.biz.ontology.auth.identity.service;

import com.biz.ontology.api.auth.dto.PermissionResponse;
import com.biz.ontology.auth.identity.model.AuthPermissionEntity;
import com.biz.ontology.auth.identity.repository.AuthPermissionRepository;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    private final AuthPermissionRepository permissionRepository;

    public PermissionService(AuthPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> list() {
        return permissionRepository.findAllByDeletedFlagFalseOrderByModuleAscCodeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuthPermissionEntity> requireAll(Set<String> submittedCodes) {
        Set<String> normalized = submittedCodes.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toUnmodifiableSet());
        List<AuthPermissionEntity> permissions = permissionRepository
                .findAllByCodeInAndDeletedFlagFalse(normalized);
        Set<String> found = permissions.stream().map(AuthPermissionEntity::getCode).collect(Collectors.toSet());
        if (!found.equals(normalized)) {
            throw new BusinessException(PlatformErrorCode.AUTH_PERMISSION_CODE_INVALID);
        }
        return permissions;
    }

    private PermissionResponse toResponse(AuthPermissionEntity entity) {
        return new PermissionResponse(entity.getId(), entity.getCode(), entity.getName(), entity.getModule());
    }
}
