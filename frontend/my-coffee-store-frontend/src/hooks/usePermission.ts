/**
 * 权限检查 Hook
 *
 * 提供基于角色和权限码的访问控制能力，
 * 配合后端 RBAC 体系使用。
 */

import { useAuth } from '../contexts/AuthContext';

export const usePermission = () => {
  const { user } = useAuth();
  const permissions = user?.permissions ?? [];

  return {
    /** 检查是否拥有指定权限 */
    hasPermission: (code: string) => permissions.includes(code),
    /** 检查是否拥有任一权限 */
    hasAnyPermission: (codes: string[]) => codes.some((c) => permissions.includes(c)),
    /** 检查是否拥有全部权限 */
    hasAllPermissions: (codes: string[]) => codes.every((c) => permissions.includes(c)),
    /** 当前用户角色 */
    role: user?.role ?? 'user',
    /** 是否为管理员 */
    isAdmin: user?.role === 'admin',
    /** 是否为员工 */
    isStaff: user?.role === 'staff',
    /** 权限列表 */
    permissions,
  };
};
