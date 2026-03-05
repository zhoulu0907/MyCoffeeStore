import React, { useState, useEffect } from 'react';
import Header from '../Header';
import LlmConfigSection from './LlmConfigSection';
import UsersSection from './UsersSection';

/**
 * 管理员页面组件
 *
 * @author zhoulu
 * @since 2026-03-05
 */
export const AdminPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'config' | 'users'>('config');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 模拟加载用户信息
    const loadUserInfo = async () => {
      try {
        // 这里应该调用 API 获取用户信息
        // const response = await fetch('/api/admin/info');
        // const data = await response.json();
        // setUser(data);
      } catch (error) {
        console.error('加载用户信息失败:', error);
      } finally {
        setLoading(false);
      }
    };

    loadUserInfo();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="container mx-auto px-4 py-8">
          <div className="flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">管理员控制台</h1>
          <p className="mt-2 text-gray-600">管理 AI 模型配置和用户信息</p>
        </div>

        {/* 标签导航 */}
        <div className="mb-6">
          <nav className="flex space-x-8 border-b border-gray-200">
            <button
              onClick={() => setActiveTab('config')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'config'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              AI 配置
            </button>
            <button
              onClick={() => setActiveTab('users')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'users'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              用户管理
            </button>
          </nav>
        </div>

        {/* 内容区域 */}
        <div className="bg-white shadow rounded-lg p-6">
          {activeTab === 'config' && <LlmConfigSection />}
          {activeTab === 'users' && <UsersSection />}
        </div>
      </div>
    </div>
  );
};