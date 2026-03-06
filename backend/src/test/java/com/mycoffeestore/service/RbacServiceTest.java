package com.mycoffeestore.service;

import com.mycoffeestore.entity.*;
import com.mycoffeestore.exception.BusinessException;
import com.mycoffeestore.mapper.*;
import com.mycoffeestore.service.impl.rbac.RbacServiceImpl;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RbacService 单元测试
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
@ExtendWith(MockitoExtension.class)
class RbacServiceTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @InjectMocks
    private RbacServiceImpl rbacService;

    private Role adminRole;
    private Role userRole;
    private Permission orderCreatePerm;
    private Permission orderViewPerm;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder().id(1L).code("admin").name("管理员").build();
        userRole = Role.builder().id(3L).code("user").name("普通用户").build();
        orderCreatePerm = Permission.builder().id(1L).code("order:create").name("创建订单").build();
        orderViewPerm = Permission.builder().id(2L).code("order:view").name("查看订单").build();
    }

    @Test
    @DisplayName("获取用户角色编码 - 用户有角色")
    void getUserRoleCode_withRole() {
        UserRole ur = UserRole.builder().userId(1L).roleId(1L).build();
        when(userRoleMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(ur);
        when(roleMapper.selectOneById(1L)).thenReturn(adminRole);

        String roleCode = rbacService.getUserRoleCode(1L);
        assertEquals("admin", roleCode);
    }

    @Test
    @DisplayName("获取用户角色编码 - 用户无角色则返回 user")
    void getUserRoleCode_noRole() {
        when(userRoleMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);

        String roleCode = rbacService.getUserRoleCode(1L);
        assertEquals("user", roleCode);
    }

    @Test
    @DisplayName("获取用户权限列表")
    void getUserPermissions() {
        UserRole ur = UserRole.builder().userId(1L).roleId(1L).build();
        when(userRoleMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(ur);

        RolePermission rp1 = RolePermission.builder().roleId(1L).permissionId(1L).build();
        RolePermission rp2 = RolePermission.builder().roleId(1L).permissionId(2L).build();
        when(rolePermissionMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(rp1, rp2));

        when(permissionMapper.selectOneById(1L)).thenReturn(orderCreatePerm);
        when(permissionMapper.selectOneById(2L)).thenReturn(orderViewPerm);

        List<String> permissions = rbacService.getUserPermissions(1L);
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains("order:create"));
        assertTrue(permissions.contains("order:view"));
    }

    @Test
    @DisplayName("获取用户权限列表 - 无角色时返回 user 角色权限")
    void getUserPermissions_noRole() {
        when(userRoleMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);

        // 查找 user 角色
        when(roleMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(userRole);

        RolePermission rp1 = RolePermission.builder().roleId(3L).permissionId(1L).build();
        when(rolePermissionMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(Collections.singletonList(rp1));
        when(permissionMapper.selectOneById(1L)).thenReturn(orderCreatePerm);

        List<String> permissions = rbacService.getUserPermissions(1L);
        assertEquals(1, permissions.size());
        assertTrue(permissions.contains("order:create"));
    }

    @Test
    @DisplayName("获取所有角色")
    void getAllRoles() {
        when(roleMapper.selectAll()).thenReturn(Arrays.asList(adminRole, userRole));

        List<Role> roles = rbacService.getAllRoles();
        assertEquals(2, roles.size());
    }

    @Test
    @DisplayName("获取所有权限")
    void getAllPermissions() {
        when(permissionMapper.selectAll()).thenReturn(Arrays.asList(orderCreatePerm, orderViewPerm));

        List<Permission> permissions = rbacService.getAllPermissions();
        assertEquals(2, permissions.size());
    }

    @Test
    @DisplayName("分配角色 - 角色不存在时抛异常")
    void assignRole_roleNotFound() {
        when(roleMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class, () -> rbacService.assignRole(1L, "nonexistent"));
    }

    @Test
    @DisplayName("分配角色 - 成功分配")
    void assignRole_success() {
        when(roleMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(adminRole);
        when(userRoleMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        when(userRoleMapper.insert(any(UserRole.class))).thenReturn(1);

        assertDoesNotThrow(() -> rbacService.assignRole(1L, "admin"));
        verify(userRoleMapper).insert(any(UserRole.class));
    }

    @Test
    @DisplayName("分配角色 - 已有角色时先删除再分配")
    void assignRole_alreadyHasRole() {
        UserRole existingUr = UserRole.builder().userId(1L).roleId(3L).build();
        when(roleMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(adminRole);
        when(userRoleMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(existingUr);
        when(userRoleMapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(1);
        when(userRoleMapper.insert(any(UserRole.class))).thenReturn(1);

        assertDoesNotThrow(() -> rbacService.assignRole(1L, "admin"));
        verify(userRoleMapper).deleteByQuery(any(QueryWrapper.class));
        verify(userRoleMapper).insert(any(UserRole.class));
    }

    @Test
    @DisplayName("更新角色权限")
    void updateRolePermissions() {
        when(roleMapper.selectOneById(1L)).thenReturn(adminRole);
        when(rolePermissionMapper.deleteByQuery(any(QueryWrapper.class))).thenReturn(3);
        when(rolePermissionMapper.insert(any(RolePermission.class))).thenReturn(1);

        assertDoesNotThrow(() -> rbacService.updateRolePermissions(1L, Arrays.asList(1L, 2L)));
        verify(rolePermissionMapper).deleteByQuery(any(QueryWrapper.class));
        verify(rolePermissionMapper, times(2)).insert(any(RolePermission.class));
    }

    @Test
    @DisplayName("更新角色权限 - 角色不存在时抛异常")
    void updateRolePermissions_roleNotFound() {
        when(roleMapper.selectOneById(99L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> rbacService.updateRolePermissions(99L, Arrays.asList(1L, 2L)));
    }
}
