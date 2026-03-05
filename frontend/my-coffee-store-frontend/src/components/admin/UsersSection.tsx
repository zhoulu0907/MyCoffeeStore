/**
 * 用户列表区域组件 - 管理员页面
 */

import React, { useState, useEffect } from 'react';
import type { AdminUser } from '../../types/admin';
import { adminApi } from '../../services/api';
import type { ApiResponse } from '../../types';
import UserRow from './UserRow';

const UsersSection: React.FC = () => {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const response = await adminApi.getUsers({ page: 1, size: 50 }) as unknown as ApiResponse<{ users: AdminUser[]; total: number }>;
        if (response.code === 200 && response.data) {
          setUsers(response.data.users || []);
        } else {
          setError('获取用户列表失败');
        }
      } catch (err) {
        console.error('获取用户列表失败:', err);
        setError('获取用户列表失败');
        // 开发环境使用模拟数据
        setUsers(getMockUsers());
      } finally {
        setIsLoading(false);
      }
    };

    fetchUsers();
  }, []);

  // 模拟数据（开发环境）
  const getMockUsers = (): AdminUser[] => [
    {
      id: 1,
      username: 'haight_user',
      email: 'user@haightcafe.com',
      phone: '+1 (415) 555-0101',
      balance: 150.00,
      orderCount: 23,
      lastOrderDate: '2026-03-05',
      lastOrderNo: 'A-2046',
      createTime: '2025-01-15',
      status: 'active',
    },
    {
      id: 2,
      username: 'emma_bean',
      email: 'emma@haightcafe.com',
      phone: '+1 (415) 555-0102',
      balance: 85.50,
      orderCount: 11,
      lastOrderDate: '2026-03-04',
      lastOrderNo: 'A-2033',
      createTime: '2025-02-20',
      status: 'active',
    },
    {
      id: 3,
      username: 'coffee_lover',
      email: 'lover@haightcafe.com',
      phone: '+1 (415) 555-0103',
      balance: 200.00,
      orderCount: 45,
      lastOrderDate: '2026-03-05',
      lastOrderNo: 'A-2047',
      createTime: '2024-12-10',
      status: 'active',
    },
    {
      id: 4,
      username: 'morning_brew',
      email: 'brew@haightcafe.com',
      phone: '+1 (415) 555-0104',
      balance: 50.00,
      orderCount: 8,
      lastOrderDate: '2026-03-03',
      lastOrderNo: 'A-2028',
      createTime: '2025-03-01',
      status: 'active',
    },
  ];

  return (
    <div
      className="rounded-xl p-[14px] flex flex-col gap-[10px]"
      style={{ backgroundColor: '#FFFFFF' }}
    >
      {/* 标题 */}
      <div>
        <h2
          className="text-[20px] font-bold mb-1"
          style={{ color: '#2A1A15', fontFamily: 'Inter, sans-serif' }}
        >
          用户列表
        </h2>
        <p
          className="text-[13px] font-normal"
          style={{ color: '#7A5A4E', fontFamily: 'Inter, sans-serif' }}
        >
          账户信息 + 订单信息
        </p>
      </div>

      {/* 用户列表 */}
      <div
        className="rounded-[10px] flex flex-col"
        style={{ backgroundColor: '#F2ECE5' }}
      >
        {/* 表头 */}
        <div
          className="flex items-center h-[42px] w-full px-3"
          style={{ backgroundColor: '#E9DED2' }}
        >
          <span
            className="text-[13px] font-bold flex-1"
            style={{ color: '#5E4338' }}
          >
            账户
          </span>
          <span
            className="text-[13px] font-bold flex-1"
            style={{ color: '#5E4338' }}
          >
            邮箱
          </span>
          <span
            className="text-[13px] font-bold text-center"
            style={{ color: '#5E4338', width: '120px' }}
          >
            订单数
          </span>
          <span
            className="text-[13px] font-bold text-right"
            style={{ color: '#5E4338', width: '220px' }}
          >
            最近订单
          </span>
        </div>

        {/* 用户数据 */}
        {isLoading ? (
          <div className="flex items-center justify-center h-[132px] text-[13px]" style={{ color: '#7A5A4E' }}>
            加载中...
          </div>
        ) : error ? (
          <div className="flex items-center justify-center h-[132px] text-[13px]" style={{ color: '#DC2626' }}>
            {error}
          </div>
        ) : users.length === 0 ? (
          <div className="flex items-center justify-center h-[132px] text-[13px]" style={{ color: '#7A5A4E' }}>
            暂无用户数据
          </div>
        ) : (
          users.map((user, index) => <UserRow key={user.id} user={user} index={index} />)
        )}
      </div>
    </div>
  );
};

export default UsersSection;
