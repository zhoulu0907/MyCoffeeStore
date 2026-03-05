/**
 * 管理员页面 - /admin
 * 包含用户列表和 LLM 配置功能
 */

import React from 'react';
import { AdminHeader, UsersSection, LlmConfigSection } from '../components/admin';

const AdminPage: React.FC = () => {
  return (
    <div
      className="min-h-screen flex flex-col"
      style={{ backgroundColor: '#F7F1E8' }}
    >
      {/* 页头 */}
      <AdminHeader />

      {/* 主体内容 */}
      <main className="flex-1 w-full">
        <div
          className="w-full mx-auto"
          style={{ maxWidth: '1200px' }}
        >
          <div className="flex flex-col gap-4 p-6">
            {/* 页面标题 */}
            <h1
              className="text-[32px] font-bold"
              style={{
                color: '#2A1A15',
                fontFamily: 'Inter, sans-serif',
              }}
            >
              管理后台
            </h1>

            {/* 用户列表区域 */}
            <UsersSection />

            {/* LLM 配置区域 */}
            <LlmConfigSection />
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminPage;
