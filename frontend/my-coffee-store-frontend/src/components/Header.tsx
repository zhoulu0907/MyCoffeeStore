/**
 * 页头组件 - Haight Ashbury Cafe
 */

import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts';
import { useCart } from '../contexts';
import { useCoffeeGuide } from '../contexts/CoffeeGuideContext';
import { userApi } from '../services/api';
import { formatPrice } from '../utils/helpers';
import { ROUTES } from '../utils/constants';
import type { ApiResponse } from '../types';

const Header: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, isAuthenticated } = useAuth();
  const { totalQuantity, totalPrice } = useCart();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [balance, setBalance] = useState<number | null>(null);
  const { isExpanded: isGuideExpanded, toggle: toggleGuide } = useCoffeeGuide();

  // 获取用户余额
  useEffect(() => {
    const fetchBalance = async () => {
      if (!isAuthenticated) {
        setBalance(null);
        return;
      }

      try {
        const response = await userApi.getBalance() as unknown as ApiResponse<{ balance: number }>;
        if (response.code === 200 && response.data) {
          setBalance(response.data.balance);
        }
      } catch (error) {
        console.error('获取余额失败:', error);
      }
    };

    fetchBalance();
  }, [isAuthenticated]);

  // 导航菜单配置
  const navItems = [
    { path: ROUTES.HOME, label: '首页' },
    { path: ROUTES.COFFEE_LIST, label: '菜单' },
  ];

  // 处理登录按钮点击
  const handleLoginClick = () => {
    navigate(ROUTES.LOGIN);
  };

  // 处理购物车点击
  const handleCartClick = () => {
    navigate(ROUTES.CART);
    setIsMobileMenuOpen(false);
  };

  // 处理导航点击
  const handleNavClick = (path: string) => {
    navigate(path);
    setIsMobileMenuOpen(false);
  };

  return (
    <header style={{ backgroundColor: '#1F130F' }}>
      <div className="max-w-7xl mx-auto px-8">
        <div className="flex items-center justify-between h-20">
          {/* Logo */}
          <Link
            to={ROUTES.HOME}
            className="text-2xl font-bold text-background hover:text-gold transition-colors"
            style={{ fontFamily: 'Inter, sans-serif' }}
            onClick={() => setIsMobileMenuOpen(false)}
          >
            Haight Ashbury Cafe
          </Link>

          {/* 桌面端导航 */}
          <nav className="hidden md:flex items-center space-x-6">
            {navItems.map((item) => (
              <span
                key={item.path}
                onClick={() => handleNavClick(item.path)}
                className={`text-base cursor-pointer transition-colors ${
                  location.pathname === item.path ? 'text-background' : 'text-gold-dark'
                } hover:text-background`}
                style={{ fontFamily: 'Inter, sans-serif' }}
              >
                {item.label}
              </span>
            ))}
          </nav>

          {/* 右侧按钮组 */}
          <div className="hidden md:flex items-center space-x-4">
            {/* 购物车按钮 */}
            <span
              onClick={handleCartClick}
              className="cursor-pointer text-gold-dark hover:text-background transition-colors"
              style={{ fontFamily: 'Inter, sans-serif' }}
            >
              购物车({totalQuantity})
            </span>

            {/* 咖啡向导按钮 - 优化触摸目标尺寸为 44px */}
            <button
              onClick={toggleGuide}
              className="px-4 h-11 rounded-full text-sm font-medium flex items-center justify-center hover:opacity-90 transition-opacity"
              style={{ backgroundColor: '#EADBC9', color: '#2A1A15' }}
            >
              {isGuideExpanded ? '收起' : '咖啡向导'}
            </button>

            {/* 登录/用户信息 */}
            {isAuthenticated ? (
              <Link
                to={ROUTES.PROFILE}
                className="text-background text-sm font-medium hover:text-gold transition-colors"
              >
                <span>{user?.username}</span>
                {balance !== null && (
                  <span className="ml-2 text-xs" style={{ color: '#D8C8B4' }}>
                    {formatPrice(balance)}
                  </span>
                )}
              </Link>
            ) : (
              <button
                onClick={handleLoginClick}
                className="text-background text-sm font-medium hover:text-gold transition-colors"
              >
                登录
              </button>
            )}
          </div>

          {/* 移动端菜单按钮 - 优化触摸目标尺寸为 44px */}
          <button
            className="md:hidden p-2.5 text-background"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            aria-label="菜单"
          >
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              {isMobileMenuOpen ? (
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              ) : (
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 6h16M4 12h16M4 18h16"
                />
              )}
            </svg>
          </button>
        </div>
      </div>

      {/* 移动端菜单 */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-t" style={{ borderColor: '#5A4036', backgroundColor: '#1F130F' }}>
          <div className="px-4 py-4 space-y-3">
            {/* 导航链接 - 优化触摸目标尺寸为 44px */}
            {navItems.map((item) => (
              <button
                key={item.path}
                onClick={() => handleNavClick(item.path)}
                className={`block w-full text-left px-4 py-3 text-base rounded-button transition-colors ${
                  location.pathname === item.path
                    ? 'text-background'
                    : 'text-gold-dark hover:text-background'
                }`}
              >
                {item.label}
              </button>
            ))}

            {/* 购物车 - 优化触摸目标尺寸为 44px */}
            <button
              onClick={handleCartClick}
              className="flex items-center justify-between w-full px-4 py-3 text-base text-gold-dark rounded-button hover:text-background transition-colors"
            >
              <span>购物车</span>
              <div className="flex items-center space-x-2">
                {totalQuantity > 0 && (
                  <span className="text-background">{totalQuantity} 件</span>
                )}
                {totalPrice > 0 && (
                  <span className="text-background">{formatPrice(totalPrice)}</span>
                )}
              </div>
            </button>

            {/* 登录/用户信息 - 优化触摸目标尺寸为 44px */}
            {isAuthenticated ? (
              <button
                onClick={() => handleNavClick(ROUTES.PROFILE)}
                className="block w-full text-left px-4 py-3 text-base text-background rounded-button hover:text-gold transition-colors"
              >
                <span>{user?.username}</span>
                {balance !== null && (
                  <span className="ml-2 text-xs" style={{ color: '#D8C8B4' }}>
                    余额：{formatPrice(balance)}
                  </span>
                )}
              </button>
            ) : (
              <button
                onClick={() => handleNavClick(ROUTES.LOGIN)}
                className="block w-full text-left px-4 py-3 text-base text-gold-dark rounded-button hover:text-background transition-colors"
              >
                登录
              </button>
            )}
          </div>
        </div>
      )}
    </header>
  );
};

export default Header;
