/**
 * 受保护的路由组件 - 用于需要认证的页面
 */

import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts';
import { ROUTES } from '../utils/constants';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAdmin?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requireAdmin = false,
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

  // 如果需要管理员权限，检查用户是否是管理员
  if (requireAdmin && user?.username !== 'admin') {
    return <Navigate to={ROUTES.HOME} replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
