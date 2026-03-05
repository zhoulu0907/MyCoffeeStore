/**
 * 用户行组件 - 管理员页面用户列表
 */

import React from 'react';
import type { AdminUser } from '../../types/admin';

interface UserRowProps {
  user: AdminUser;
  index: number;
}

const UserRow: React.FC<UserRowProps> = ({ user, index }) => {
  // 交替背景色
  const bgColor = index % 2 === 0 ? '#FFFFFF' : '#FCF9F5';

  // 格式化日期
  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    } catch {
      return dateString;
    }
  };

  return (
    <div
      className="flex items-center h-[44px] w-full px-3"
      style={{ backgroundColor: bgColor }}
    >
      <span
        className="text-[13px] font-normal flex-1"
        style={{ color: '#2A1A15' }}
      >
        {user.username}
      </span>
      <span
        className="text-[13px] font-normal flex-1"
        style={{ color: '#2A1A15' }}
      >
        {user.email}
      </span>
      <span
        className="text-[13px] font-normal text-center"
        style={{ color: '#2A1A15', width: '120px' }}
      >
        {user.orderCount}
      </span>
      <span
        className="text-[13px] font-normal text-right"
        style={{ color: '#2A1A15', width: '220px' }}
      >
        {user.lastOrderNo
          ? `${formatDate(user.lastOrderDate)} #${user.lastOrderNo}`
          : '-'}
      </span>
    </div>
  );
};

export default UserRow;
