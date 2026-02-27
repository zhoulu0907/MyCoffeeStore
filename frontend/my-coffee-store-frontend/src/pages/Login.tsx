/**
 * 登录页面
 */

import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts';
import { ROUTES } from '../utils/constants';
import { userApi } from '../services/api';
import type { ApiResponse } from '../types';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    account: '',
    password: '',
  });

  const [errors, setErrors] = useState<{
    account?: string;
    password?: string;
    general?: string;
  }>({});

  const [isLoading, setIsLoading] = useState(false);

  // 处理输入变化
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // 清除对应字段的错误
    setErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  // 验证表单
  const validateForm = (): boolean => {
    const newErrors: typeof errors = {};

    if (!formData.account.trim()) {
      newErrors.account = '请输入用户名或邮箱';
    }

    if (!formData.password) {
      newErrors.password = '请输入密码';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 处理提交
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    setErrors({});

    try {
      // 调用后端登录 API
      const response = await userApi.login({
        account: formData.account,
        password: formData.password,
      }) as unknown as ApiResponse;

      if (response.code === 200) {
        // 登录成功，从响应中提取 token 和用户信息
        const { token, userId, username, email, phone } = response.data;
        const userData = { id: userId, username, email, phone, createTime: '' };
        login(userData, token);
        navigate(ROUTES.HOME);
      } else {
        setErrors({
          general: response.message || '登录失败，请检查您的账号和密码',
        });
      }
    } catch (error: any) {
      setErrors({
        general: error?.response?.data?.message || '登录失败，请检查您的账号和密码',
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col" style={{ backgroundColor: '#F3EBE2' }}>
      {/* 简化的页头 */}
      <div className="bg-white h-20 flex items-center px-6">
        <Link to={ROUTES.HOME} className="font-georgia text-2xl font-bold text-primary">
          MyCoffeeStore
        </Link>
      </div>

      {/* 登录表单 */}
      <div className="flex-1 flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-md">
          {/* 标题 */}
          <div className="text-center mb-8">
            <h1 className="font-georgia font-bold mb-2" style={{ fontSize: '36px', color: '#2A1A15' }}>
              欢迎回来
            </h1>
            <p style={{ fontSize: '15px', color: '#3D3D3D' }}>
              登录您的账户
            </p>
          </div>

          {/* 表单 */}
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 用户名/邮箱 */}
            <div>
              <label htmlFor="account" className="block text-sm font-medium mb-2" style={{ color: '#2A1A15' }}>
                邮箱/用户名
              </label>
              <input
                type="text"
                id="account"
                name="account"
                value={formData.account}
                onChange={handleInputChange}
                className="w-full bg-white transition-all focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed"
                style={{
                  height: '48px',
                  padding: '0 16px',
                  border: `1px solid ${errors.account ? '#EF4444' : '#D8CFC3'}`,
                  borderRadius: '12px',
                  color: '#2A1A15',
                }}
                placeholder="请输入用户名或邮箱"
                disabled={isLoading}
              />
              {errors.account && (
                <p className="mt-1 text-sm text-red-500">{errors.account}</p>
              )}
            </div>

            {/* 密码 */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium mb-2" style={{ color: '#2A1A15' }}>
                密码
              </label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                className="w-full bg-white transition-all focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed"
                style={{
                  height: '48px',
                  padding: '0 16px',
                  border: `1px solid ${errors.password ? '#EF4444' : '#D8CFC3'}`,
                  borderRadius: '12px',
                  color: '#2A1A15',
                }}
                placeholder="请输入密码"
                disabled={isLoading}
              />
              {errors.password && (
                <p className="mt-1 text-sm text-red-500">{errors.password}</p>
              )}
            </div>

            {/* 通用错误 */}
            {errors.general && (
              <div className="text-sm px-4 py-3" style={{
                backgroundColor: '#FEF2F2',
                border: '1px solid #FCA5A5',
                color: '#DC2626',
                borderRadius: '12px',
              }}>
                {errors.general}
              </div>
            )}

            {/* 登录按钮 */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full font-medium transition-all disabled:cursor-not-allowed"
              style={{
                height: '48px',
                backgroundColor: isLoading ? 'rgba(31, 19, 15, 0.5)' : '#1F130F',
                color: '#FFFFFF',
                borderRadius: '12px',
              }}
            >
              {isLoading ? '登录中...' : '登录'}
            </button>

            {/* 底部链接 */}
            <div className="text-center space-y-2">
              <div className="text-sm" style={{ color: '#5B4035' }}>
                还没有账户？{' '}
                <Link
                  to={ROUTES.REGISTER}
                  className="font-medium hover:underline"
                  style={{ color: '#D4A574' }}
                >
                  立即注册
                </Link>
              </div>
              <Link
                to="/forgot-password"
                className="block text-sm hover:underline"
                style={{ color: '#5B4035' }}
              >
                忘记密码？
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
