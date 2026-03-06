/**
 * 权限守卫组件
 *
 * 根据用户权限有条件地渲染子组件。
 * 支持单个权限码或权限码数组（任一匹配即通过）。
 */

import type { ReactNode } from 'react';
import { usePermission } from '../hooks/usePermission';

interface PermissionGuardProps {
  /** 需要的权限码，字符串为单权限检查，数组为任一权限匹配 */
  permission: string | string[];
  /** 有权限时渲染的内容 */
  children: ReactNode;
  /** 无权限时的回退内容 */
  fallback?: ReactNode;
}

const PermissionGuard = ({
  permission,
  children,
  fallback = null,
}: PermissionGuardProps) => {
  const { hasPermission, hasAnyPermission } = usePermission();

  const permitted = Array.isArray(permission)
    ? hasAnyPermission(permission)
    : hasPermission(permission);

  return permitted ? <>{children}</> : <>{fallback}</>;
};

export default PermissionGuard;
