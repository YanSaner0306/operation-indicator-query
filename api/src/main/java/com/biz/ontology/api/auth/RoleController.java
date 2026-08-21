/**
 * 模块3：角色管理REST API。
 * 功能：提供分页、创建、编辑、状态控制和权限替换。
 * 技术栈：Spring MVC + Bean Validation + Spring方法安全（使用AUTH_MANAGE）。
 */
package com.biz.ontology.api.auth;

import com.biz.ontology.api.auth.dto.CreateRoleRequest;
import com.biz.ontology.api.auth.dto.RoleResponse;
import com.biz.ontology.api.auth.dto.SetRolePermissionsRequest;
import com.biz.ontology.api.auth.dto.UpdateAuthStatusRequest;
import com.biz.ontology.api.auth.dto.UpdateRoleRequest;
import com.biz.ontology.api.common.PageResponse;
import com.biz.ontology.api.common.R;
import com.biz.ontology.auth.identity.model.AuthStatus;
import com.biz.ontology.auth.identity.service.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/auth/roles")
@PreAuthorize("hasAuthority('AUTH_MANAGE')")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public R<PageResponse<RoleResponse>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AuthStatus status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return R.ok(roleService.page(keyword, status, page, size));
    }

    @PostMapping
    public R<RoleResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        return R.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    public R<RoleResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return R.ok(roleService.update(id, request));
    }

    @PatchMapping("/{id}/enabled")
    public R<RoleResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateAuthStatusRequest request) {
        return R.ok(roleService.updateStatus(id, request));
    }

    @PutMapping("/{id}/permissions")
    public R<RoleResponse> setPermissions(
            @PathVariable Long id,
            @Valid @RequestBody SetRolePermissionsRequest request
    ) {
        return R.ok(roleService.setPermissions(id, request));
    }
}
