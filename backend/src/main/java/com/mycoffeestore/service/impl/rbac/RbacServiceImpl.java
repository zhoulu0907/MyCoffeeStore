package com.mycoffeestore.service.impl.rbac;

import com.mycoffeestore.config.CacheConfig;
import com.mycoffeestore.entity.*;
import com.mycoffeestore.exception.BusinessException;
import com.mycoffeestore.mapper.*;
import com.mycoffeestore.service.rbac.RbacService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * RBAC 权限管理服务实现类
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public String getUserRoleCode(Long userId) {
        UserRole userRole = userRoleMapper.selectOneByQuery(
                QueryWrapper.create().eq(UserRole::getUserId, userId));

        if (userRole == null) {
            return "user"; // 默认角色
        }

        Role role = roleMapper.selectOneById(userRole.getRoleId());
        if (role == null) {
            return "user";
        }

        return role.getCode();
    }

    @Override
    @Cacheable(value = "userPermissions", key = "#userId", unless = "#result.isEmpty()")
    public List<String> getUserPermissions(Long userId) {
        // 获取用户角色
        UserRole userRole = userRoleMapper.selectOneByQuery(
                QueryWrapper.create().eq(UserRole::getUserId, userId));

        Long roleId;
        if (userRole != null) {
            roleId = userRole.getRoleId();
        } else {
            // 无角色分配时，使用默认 user 角色
            Role defaultRole = roleMapper.selectOneByQuery(
                    QueryWrapper.create().eq(Role::getCode, "user"));
            if (defaultRole == null) {
                return Collections.emptyList();
            }
            roleId = defaultRole.getId();
        }

        // 获取角色关联的权限ID
        List<RolePermission> rolePermissions = rolePermissionMapper.selectListByQuery(
                QueryWrapper.create().eq(RolePermission::getRoleId, roleId));

        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询权限详情（修复 N+1 问题）
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        List<Permission> permissions = permissionMapper.selectListByQuery(
                QueryWrapper.create().in(Permission::getId, permissionIds));

        return permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.selectAll();
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionMapper.selectAll();
    }

    @Override
    @Transactional
    @CacheConfig.ClearUserPermissionsCache
    public void assignRole(Long userId, String roleCode) {
        // 参数验证
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new BusinessException(400, "角色编码不能为空");
        }

        // 查找角色
        Role role = roleMapper.selectOneByQuery(
                QueryWrapper.create().eq(Role::getCode, roleCode));
        if (role == null) {
            throw new BusinessException(404, "角色不存在: " + roleCode);
        }

        // 直接删除已有角色分配（无需先查询）
        userRoleMapper.deleteByQuery(
                QueryWrapper.create().eq(UserRole::getUserId, userId));

        // 分配新角色
        UserRole userRole = UserRole.builder()
                .userId(userId)
                .roleId(role.getId())
                .build();
        userRoleMapper.insert(userRole);

        log.info("角色分配成功: userId={}, roleCode={}", userId, roleCode);
    }

    @Override
    @Transactional
    @CacheConfig.ClearUserPermissionsCache
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        // 参数验证
        if (permissionIds == null) {
            throw new BusinessException(400, "权限ID列表不能为空");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectOneById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在: " + roleId);
        }

        // 验证权限ID是否有效
        if (!permissionIds.isEmpty()) {
            long validCount = permissionMapper.selectCountByQuery(
                    QueryWrapper.create().in(Permission::getId, permissionIds));
            if (validCount != permissionIds.size()) {
                throw new BusinessException(400, "存在无效的权限ID");
            }
        }

        // 删除旧的权限关联
        rolePermissionMapper.deleteByQuery(
                QueryWrapper.create().eq(RolePermission::getRoleId, roleId));

        // 批量插入新的权限关联
        if (!permissionIds.isEmpty()) {
            List<RolePermission> rolePermissions = permissionIds.stream()
                    .map(permissionId -> RolePermission.builder()
                            .roleId(roleId)
                            .permissionId(permissionId)
                            .build())
                    .collect(Collectors.toList());
            rolePermissions.forEach(rolePermissionMapper::insert);
        }

        log.info("角色权限更新成功: roleId={}, permissionCount={}", roleId, permissionIds.size());
    }

    @Override
    public List<Permission> getRolePermissions(Long roleId) {
        List<RolePermission> rolePermissions = rolePermissionMapper.selectListByQuery(
                QueryWrapper.create().eq(RolePermission::getRoleId, roleId));

        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询权限详情（修复 N+1 问题）
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        return permissionMapper.selectListByQuery(
                QueryWrapper.create().in(Permission::getId, permissionIds));
    }
}
