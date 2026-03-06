/**
 * 管理员页面 - /admin
 * 基于权限动态展示管理标签页
 */

import React, { useState, useMemo } from 'react';
import { AdminHeader, UsersSection, LlmConfigSection } from '../components/admin';
import { usePermission } from '../hooks/usePermission';

/** 标签页配置 */
interface TabConfig {
  key: string;
  label: string;
  permission: string;
}

/** 全部可用标签页 */
const allTabs: TabConfig[] = [
  { key: 'users', label: '用户管理', permission: 'user:view' },
  { key: 'llm', label: 'AI 配置', permission: 'llm:config' },
  { key: 'roles', label: '角色权限', permission: 'role:manage' },
];

const AdminPage: React.FC = () => {
  const { hasPermission } = usePermission();

  // 根据权限过滤出可见标签页
  const visibleTabs = useMemo(
    () => allTabs.filter((tab) => hasPermission(tab.permission)),
    [hasPermission],
  );

  // 默认激活第一个可见标签页
  const defaultTab = visibleTabs.length > 0 ? visibleTabs[0].key : '';
  const [activeTab, setActiveTab] = useState<string>(defaultTab);

  // 确保 activeTab 始终在可见标签页中（权限变化时自动回退）
  const effectiveTab = visibleTabs.some((t) => t.key === activeTab)
    ? activeTab
    : defaultTab;

  // 渲染标签页内容
  const renderTabContent = () => {
    switch (effectiveTab) {
      case 'users':
        return <UsersSection />;
      case 'llm':
        return <LlmConfigSection />;
      case 'roles':
        return (
          <div
            className="rounded-xl p-6 text-center"
            style={{ backgroundColor: '#FFFFFF', color: '#7A5A4E' }}
          >
            角色权限管理功能即将上线
          </div>
        );
      default:
        return null;
    }
  };

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

            {/* 标签导航 */}
            {visibleTabs.length > 0 ? (
              <>
                <nav className="flex space-x-6 border-b" style={{ borderColor: '#E9DED2' }}>
                  {visibleTabs.map((tab) => (
                    <button
                      key={tab.key}
                      onClick={() => setActiveTab(tab.key)}
                      className="pb-2 text-[14px] font-medium transition-colors border-b-2"
                      style={{
                        borderColor: effectiveTab === tab.key ? '#5B4035' : 'transparent',
                        color: effectiveTab === tab.key ? '#2A1A15' : '#7A5A4E',
                        fontFamily: 'Inter, sans-serif',
                      }}
                    >
                      {tab.label}
                    </button>
                  ))}
                </nav>

                {/* 标签页内容 */}
                {renderTabContent()}
              </>
            ) : (
              <div
                className="rounded-xl p-6 text-center"
                style={{ backgroundColor: '#FFFFFF', color: '#7A5A4E' }}
              >
                您没有任何管理权限
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminPage;
