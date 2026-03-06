package com.mycoffeestore.service.rbac;

import com.mycoffeestore.entity.Permission;
import com.mycoffeestore.entity.Role;

import java.util.List;

/**
 * RBAC 权限管理服务接口
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
public interface RbacService {

    /**
     * 获取用户角色编码
     *
     * @param userId 用户ID
     * @return 角色编码（如 admin, staff, user）
     */
    String getUserRoleCode(Long userId);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取所有角色
     *
     * @return 角色列表
     */
    List<Role> getAllRoles();

    /**
     * 获取所有权限
     *
     * @return 权限列表
     */
    List<Permission> getAllPermissions();

    /**
     * 分配角色给用户
     *
     * @param userId   用户ID
     * @param roleCode 角色编码
     */
    void assignRole(Long userId, String roleCode);

    /**
     * 更新角色的权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    void updateRolePermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getRolePermissions(Long roleId);
}
