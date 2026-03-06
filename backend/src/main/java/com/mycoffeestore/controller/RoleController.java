package com.mycoffeestore.controller;

import com.mycoffeestore.annotation.RequirePermission;
import com.mycoffeestore.common.result.Result;
import com.mycoffeestore.dto.rbac.AssignRoleDTO;
import com.mycoffeestore.dto.rbac.UpdateRolePermissionsDTO;
import com.mycoffeestore.entity.Permission;
import com.mycoffeestore.entity.Role;
import com.mycoffeestore.service.rbac.RbacService;
import com.mycoffeestore.vo.rbac.PermissionVO;
import com.mycoffeestore.vo.rbac.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
@Slf4j
@RestController
@RequestMapping("/v1/role")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色和权限管理接口")
@RequirePermission("role:manage")
public class RoleController {

    private final RbacService rbacService;

    @GetMapping("/list")
    @Operation(summary = "获取角色列表", description = "获取所有角色")
    public Result<List<RoleVO>> list() {
        List<Role> roles = rbacService.getAllRoles();
        List<RoleVO> voList = roles.stream()
                .map(role -> RoleVO.builder()
                        .id(role.getId())
                        .code(role.getCode())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @GetMapping("/permissions")
    @Operation(summary = "获取角色权限", description = "获取指定角色的权限列表")
    public Result<List<PermissionVO>> permissions(@RequestParam Long roleId) {
        List<Permission> permissions = rbacService.getRolePermissions(roleId);
        List<PermissionVO> voList = permissions.stream()
                .map(p -> PermissionVO.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @PostMapping("/assign")
    @Operation(summary = "分配角色", description = "给用户分配角色")
    public Result<Void> assign(@Valid @RequestBody AssignRoleDTO dto) {
        rbacService.assignRole(dto.getUserId(), dto.getRoleCode());
        return Result.success("角色分配成功", null);
    }

    @PostMapping("/permissions/update")
    @Operation(summary = "更新角色权限", description = "更新指定角色的权限列表")
    public Result<Void> updatePermissions(@Valid @RequestBody UpdateRolePermissionsDTO dto) {
        rbacService.updateRolePermissions(dto.getRoleId(), dto.getPermissionIds());
        return Result.success("权限更新成功", null);
    }
}
