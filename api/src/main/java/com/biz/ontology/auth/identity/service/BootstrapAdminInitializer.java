/**
 * 模块4-5：首次部署管理员初始化器。
 * 功能：仅在环境变量提供初始密码且系统尚无指定用户时，创建拥有全部当前权限的管理员账号。
 * 技术栈：Spring Boot ApplicationRunner、配置属性与既有RBAC应用服务。
 */
package com.biz.ontology.auth.identity.service;

import com.biz.ontology.api.auth.dto.CreateRoleRequest;
import com.biz.ontology.api.auth.dto.CreateUserRequest;
import com.biz.ontology.auth.identity.repository.*;
import com.biz.ontology.auth.token.SecurityTokenProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {
    private static final String ROLE_CODE = "PLATFORM_ADMIN";
    private final SecurityTokenProperties properties; private final AuthUserRepository users; private final AuthRoleRepository roles;
    private final PermissionService permissions; private final RoleService roleService; private final UserService userService;
    public BootstrapAdminInitializer(SecurityTokenProperties a, AuthUserRepository b, AuthRoleRepository c,
                                     PermissionService d, RoleService e, UserService f) {
        properties=a; users=b; roles=c; permissions=d; roleService=e; userService=f;
    }

    @Override
    public void run(ApplicationArguments args) {
        String password=properties.getBootstrapAdminPassword(); String username=properties.getBootstrapAdminUsername().trim().toLowerCase();
        if (password == null || password.isBlank() || users.existsByUsernameAndDeletedFlagFalse(username)) return;
        Set<String> allPermissions=permissions.list().stream().map(value -> value.code()).collect(Collectors.toSet());
        Long roleId=roles.findByCodeAndDeletedFlagFalse(ROLE_CODE).map(value -> value.getId()).orElseGet(() ->
                roleService.create(new CreateRoleRequest(ROLE_CODE,"平台管理员",allPermissions)).id());
        userService.create(new CreateUserRequest(username,"平台管理员",password,Set.of(roleId)));
    }
}
