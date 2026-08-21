/**
 * 模块3：权限字典REST API。
 * 功能：返回角色配置页面所使用的不可变后端权限目录。
 * 技术栈：Spring MVC + Spring方法安全（使用AUTH_MANAGE）。
 */
package com.biz.ontology.api.auth;

import com.biz.ontology.api.auth.dto.PermissionResponse;
import com.biz.ontology.api.common.R;
import com.biz.ontology.auth.identity.service.PermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/permissions")
@PreAuthorize("hasAuthority('AUTH_MANAGE')")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public R<List<PermissionResponse>> list() {
        return R.ok(permissionService.list());
    }
}
