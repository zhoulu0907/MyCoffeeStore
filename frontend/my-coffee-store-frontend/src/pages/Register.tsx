/**
 * 注册页面
 */

import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts';
import { ROUTES } from '../utils/constants';
import { isValidEmail, isValidPhone, validatePassword } from '../utils/helpers';
import { userApi } from '../services/api';
import type { ApiResponse } from '../types';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    username: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    agreeTerms: false,
  });

  const [errors, setErrors] = useState<{
    username?: string;
    email?: string;
    phone?: string;
    password?: string;
    confirmPassword?: string;
    agreeTerms?: string;
    general?: string;
  }>({});

  const [isLoading, setIsLoading] = useState(false);

  // 处理输入变化
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    // 清除对应字段的错误
    setErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  // 验证表单
  const validateForm = (): boolean => {
    const newErrors: typeof errors = {};

    // 用户名验证
    if (!formData.username.trim()) {
      newErrors.username = '请输入用户名';
    } else if (formData.username.length < 3) {
      newErrors.username = '用户名至少需要3个字符';
    }

    // 邮箱验证
    if (!formData.email) {
      newErrors.email = '请输入邮箱';
    } else if (!isValidEmail(formData.email)) {
      newErrors.email = '请输入有效的邮箱地址';
    }

    // 手机号验证（可选）
    if (formData.phone && !isValidPhone(formData.phone)) {
      newErrors.phone = '请输入有效的手机号';
    }

    // 密码验证
    if (!formData.password) {
      newErrors.password = '请输入密码';
    } else {
      const passwordValidation = validatePassword(formData.password);
      if (!passwordValidation.isValid) {
        newErrors.password = passwordValidation.message;
      }
    }

    // 确认密码验证
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = '请确认密码';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = '两次输入的密码不一致';
    }

    // 同意条款验证
    if (!formData.agreeTerms) {
      newErrors.agreeTerms = '请阅读并同意服务条款';
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
      // 调用后端注册 API
      const response = await userApi.register({
        username: formData.username,
        email: formData.email,
        phone: formData.phone || undefined,
        password: formData.password,
      }) as unknown as ApiResponse;

      if (response.code === 200) {
        // 注册成功，从响应中提取 token 和用户信息
        const { token, userId, username, email, phone } = response.data;
        const userData = { id: userId, username, email, phone, createTime: '' };
        login(userData, token);
        navigate(ROUTES.HOME);
      } else {
        setErrors({
          general: response.message || '注册失败，请稍后重试',
        });
      }
    } catch (error: any) {
      setErrors({
        general: error?.response?.data?.message || '注册失败，请稍后重试',
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col" style={{ backgroundColor: '#F7F1E8' }}>
      {/* 深咖啡色头部区域 */}
      <div
        className="flex items-center"
        style={{ backgroundColor: '#1F130F', height: '84px', padding: '0 20px' }}
      >
        <Link
          to={ROUTES.HOME}
          className="font-georgia text-2xl font-bold hover:opacity-90 transition-opacity"
          style={{ color: '#F7F1E8' }}
        >
          MyCoffeeStore
        </Link>
      </div>

      {/* 注册表单 */}
      <div className="flex-1 flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-md">
          {/* 白色表单卡片 */}
          <div
            className="p-8"
            style={{
              backgroundColor: '#FFFFFF',
              borderRadius: '16px',
              boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
            }}
          >
            {/* 标题 */}
            <div className="text-center mb-8">
              <h1
                className="font-georgia text-3xl font-bold mb-2"
                style={{ color: '#1F130F' }}
              >
                创建账户
              </h1>
              <p style={{ color: '#5B4035' }}>加入我们的社区</p>
            </div>

            {/* 表单 */}
            <form onSubmit={handleSubmit} className="space-y-5">
              {/* 用户名 */}
              <div>
                <label htmlFor="username" className="block text-sm font-medium mb-2" style={{ color: '#2A1A15' }}>
                  用户名 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  id="username"
                  name="username"
                  value={formData.username}
                  onChange={handleInputChange}
                  className={`w-full px-4 py-3 focus:outline-none transition-all ${
                    errors.username ? 'border-red-500 focus:ring-2 focus:ring-red-500' : ''
                  }`}
                  style={{
                    backgroundColor: '#FAFAF8',
                    border: `1px solid ${errors.username ? '#EF4444' : '#E5E4E1'}`,
                    borderRadius: '12px',
                    color: '#2A1A15',
                  }}
                  placeholder="请输入用户名"
                  disabled={isLoading}
                />
                {errors.username && (
                  <p className="mt-1 text-sm text-red-500">{errors.username}</p>
                )}
              </div>

              {/* 邮箱 */}
              <div>
                <label htmlFor="email" className="block text-sm font-medium mb-2" style={{ color: '#2A1A15' }}>
                  邮箱 <span className="text-red-500">*</span>
                </label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className={`w-full px-4 py-3 focus:outline-none transition-all ${
                    errors.email ? 'border-red-500 focus:ring-2 focus:ring-red-500' : ''
                  }`}
                  style={{
                    backgroundColor: '#FAFAF8',
                    border: `1px solid ${errors.email ? '#EF4444' : '#E5E4E1'}`,
                    borderRadius: '12px',
                    color: '#2A1A15',
                  }}
                  placeholder="请输入邮箱"
                  disabled={isLoading}
                />
                {errors.email && (
                  <p className="mt-1 text-sm text-red-500">{errors.email}</p>
                )}
              </div>

              {/* 手机号 */}
              <div>
                <label htmlFor="phone" className="block text-sm font-medium mb-2" style={{ color: '#2A1A15' }}>
                  手机号
                </label>
                <input
                  type="tel"
                  id="phone"
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                  className={`w-full px-4 py-3 focus:outline-none transition-all ${
                    errors.phone ? 'border-red-500 focus:ring-2 focus:ring-red-500' : ''
                  }`}
                  style={{
                    backgroundColor: '#FAFAF8',
                    border: `1px solid ${errors.phone ? '#EF4444' : '#E5E4E1'}`,
                    borderRadius: '12px',
                    color: '#2A1A15',
                  }}
                  placeholder="请输入手机号（可选）"
                  disabled={isLoading}
                />
                {errors.phone && (
                  <p className="mt-1 text-sm text-red-500">{errors.phone}</p>
                )}
              </div>

              {/* 密码 */}
              <div>
                <label htmlFor="password" className="block text-sm font-medium mb-2" style={{ color: '#2A1A15' }}>
                  密码 <span className="text-red-500">*</span>
                </label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  className={`w-full px-4 py-3 focus:outline-none transition-all ${
                    errors.password ? 'border-red-500 focus:ring-2 focus:ring-red-500' : ''
                  }`}
                  style={{
                    backgroundColor: '#FAFAF8',
                    border: `1px solid ${errors.password ? '#EF4444' : '#E5E4E1'}`,
                    borderRadius: '12px',
                    color: '#2A1A15',
                  }}
                  placeholder="请输入密码（至少6位）"
                  disabled={isLoading}
                />
                {errors.password && (
                  <p className="mt-1 text-sm text-red-500">{errors.password}</p>
                )}
              </div>

              {/* 确认密码 */}
              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium mb-2" style={{ color: '#2A1A15' }}>
                  确认密码 <span className="text-red-500">*</span>
                </label>
                <input
                  type="password"
                  id="confirmPassword"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  className={`w-full px-4 py-3 focus:outline-none transition-all ${
                    errors.confirmPassword ? 'border-red-500 focus:ring-2 focus:ring-red-500' : ''
                  }`}
                  style={{
                    backgroundColor: '#FAFAF8',
                    border: `1px solid ${errors.confirmPassword ? '#EF4444' : '#E5E4E1'}`,
                    borderRadius: '12px',
                    color: '#2A1A15',
                  }}
                  placeholder="请再次输入密码"
                  disabled={isLoading}
                />
                {errors.confirmPassword && (
                  <p className="mt-1 text-sm text-red-500">{errors.confirmPassword}</p>
                )}
              </div>

              {/* 同意条款 */}
              <div className="flex items-start">
                <input
                  type="checkbox"
                  id="agreeTerms"
                  name="agreeTerms"
                  checked={formData.agreeTerms}
                  onChange={handleInputChange}
                  className="mt-1 w-4 h-4 border-gray-300 rounded focus:ring-2 focus:ring-offset-0 transition-all cursor-pointer disabled:cursor-not-allowed disabled:opacity-50"
                  style={{
                    accentColor: '#3D8A5A',
                  }}
                  disabled={isLoading}
                />
                <label htmlFor="agreeTerms" className="ml-2 text-sm" style={{ color: '#5B4035' }}>
                  我已阅读并同意{' '}
                  <Link to="/terms" className="hover:underline" style={{ color: '#3D8A5A' }}>
                    服务条款
                  </Link>
                  {' '}和{' '}
                  <Link to="/privacy" className="hover:underline" style={{ color: '#3D8A5A' }}>
                    隐私政策
                  </Link>
                </label>
              </div>
              {errors.agreeTerms && (
                <p className="mt-1 text-sm text-red-500">{errors.agreeTerms}</p>
              )}

              {/* 通用错误 */}
              {errors.general && (
                <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-button text-sm">
                  {errors.general}
                </div>
              )}

              {/* 注册按钮 */}
              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-3 font-medium transition-all disabled:opacity-50 disabled:cursor-not-allowed hover:opacity-90"
                style={{
                  backgroundColor: '#1F130F',
                  color: '#FFFFFF',
                  borderRadius: '12px',
                }}
              >
                {isLoading ? '注册中...' : '注册'}
              </button>

              {/* 底部链接 */}
              <div className="text-center">
                <p className="text-sm" style={{ color: '#5B4035' }}>
                  已有账户？{' '}
                  <Link
                    to={ROUTES.LOGIN}
                    className="font-medium hover:underline"
                    style={{ color: '#3D8A5A' }}
                  >
                    立即登录
                  </Link>
                </p>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
