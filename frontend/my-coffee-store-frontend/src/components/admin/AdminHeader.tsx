/**
 * 管理员页面页头组件
 */

import React from 'react';
import { Link } from 'react-router-dom';
import { ROUTES } from '../../utils/constants';

const AdminHeader: React.FC = () => {
  const navItems = [
    { path: ROUTES.HOME, label: '首页' },
    { path: ROUTES.COFFEE_LIST, label: '菜单' },
    { path: ROUTES.ORDER, label: '订单' },
    { path: ROUTES.PROFILE, label: '管理后台' },
  ];

  return (
    <header style={{ backgroundColor: '#1F130F' }}>
      <div className="w-full px-8">
        <div className="flex items-center justify-between h-[84px]">
          {/* Logo */}
          <Link
            to={ROUTES.HOME}
            className="text-[22px] font-bold"
            style={{ color: '#F7F1E8', fontFamily: 'Inter, sans-serif' }}
          >
            Haight Ashbury Cafe
          </Link>

          {/* 导航菜单 */}
          <nav className="flex items-center space-x-6">
            {navItems.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                className="text-[14px] font-normal transition-colors hover:opacity-80"
                style={{
                  color: '#D8C8B4',
                  fontFamily: 'Inter, sans-serif',
                }}
              >
                {item.label}
              </Link>
            ))}
          </nav>
        </div>
      </div>
    </header>
  );
};

export default AdminHeader;
