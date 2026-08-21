/**
 * 模块3：用户管理REST API。
 * 功能：提供分页的CRUD相关操作、状态控制、密码重置和角色分配。
 * 技术栈：Spring MVC + Bean Validation + Spring方法安全（使用AUTH_MANAGE）。
 */
package com.biz.ontology.api.auth;

import com.biz.ontology.api.auth.dto.CreateUserRequest;
import com.biz.ontology.api.auth.dto.ResetPasswordRequest;
import com.biz.ontology.api.auth.dto.UpdateAuthStatusRequest;
import com.biz.ontology.api.auth.dto.UpdateUserRequest;
import com.biz.ontology.api.auth.dto.UserResponse;
import com.biz.ontology.api.common.PageResponse;
import com.biz.ontology.api.common.R;
import com.biz.ontology.auth.identity.model.AuthStatus;
import com.biz.ontology.auth.identity.service.UserService;
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
@RequestMapping("/api/v1/auth/users")
@PreAuthorize("hasAuthority('AUTH_MANAGE')")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public R<PageResponse<UserResponse>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AuthStatus status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return R.ok(userService.page(keyword, status, page, size));
    }

    @PostMapping
    public R<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return R.ok(userService.create(request));
    }

    @PutMapping("/{id}")
    public R<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return R.ok(userService.update(id, request));
    }

    @PatchMapping("/{id}/enabled")
    public R<UserResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateAuthStatusRequest request) {
        return R.ok(userService.updateStatus(id, request));
    }

    @PostMapping("/{id}/password/reset")
    public R<UserResponse> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        return R.ok(userService.resetPassword(id, request));
    }
}
