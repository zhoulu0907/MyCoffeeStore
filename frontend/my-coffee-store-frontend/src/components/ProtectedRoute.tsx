/**
 * 受保护的路由组件 - 用于需要认证的页面
 *
 * 支持通过 requiredPermission 进行权限检查，
 * 也支持通过 requiredRole 进行角色检查。
 */

import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts';
import { ROUTES } from '../utils/constants';

interface ProtectedRouteProps {
  children: React.ReactNode;
  /** 需要的权限码，用户必须拥有此权限才能访问 */
  requiredPermission?: string;
  /** 需要的角色列表，用户角色在其中即可访问 */
  requiredRole?: Array<'user' | 'staff' | 'admin'>;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredPermission,
  requiredRole,
}) => {
  const { isAuthenticated, user, isLoading } = useAuth();

  // 显示加载状态
  if (isLoading) {
    return (
      <div
        className="flex items-center justify-center min-h-screen"
        style={{ backgroundColor: '#F7F1E8' }}
      >
        <div
          className="text-lg font-normal"
          style={{ color: '#5B4035', fontFamily: 'Inter, sans-serif' }}
        >
          加载中...
        </div>
      </div>
    );
  }

  // 未登录则重定向到登录页
  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  // 检查权限
  if (requiredPermission && !user?.permissions?.includes(requiredPermission)) {
    return <Navigate to={ROUTES.HOME} replace />;
  }

  // 检查角色
  if (requiredRole && user?.role && !requiredRole.includes(user.role)) {
    return <Navigate to={ROUTES.HOME} replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
