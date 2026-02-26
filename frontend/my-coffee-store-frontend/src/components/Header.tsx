/**
 * 页头组件
 */

import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts';
import { useCart } from '../contexts';
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
    // { path: '/about', label: '关于我们' },
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
    <header className="bg-primary text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-20">
          {/* Logo */}
          <Link
            to={ROUTES.HOME}
            className="font-georgia text-2xl font-bold text-white hover:text-accent transition-colors"
            onClick={() => setIsMobileMenuOpen(false)}
          >
            MyCoffeeStore
          </Link>

          {/* 桌面端导航 */}
          <nav className="hidden md:flex items-center space-x-10">
            {navItems.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                className={`text-base font-medium transition-colors hover:text-accent ${
                  location.pathname === item.path ? 'text-accent' : 'text-white'
                }`}
              >
                {item.label}
              </Link>
            ))}
          </nav>

          {/* 右侧按钮组 */}
          <div className="hidden md:flex items-center space-x-4">
            {/* 购物车按钮 */}
            <button
              onClick={handleCartClick}
              className="relative p-2 text-white hover:text-accent transition-colors"
              aria-label="购物车"
            >
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
                />
              </svg>
              {totalQuantity > 0 && (
                <span className="absolute -top-1 -right-1 bg-accent text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                  {totalQuantity}
                </span>
              )}
            </button>

            {/* 登录/用户信息按钮 */}
            {isAuthenticated ? (
              <Link
                to={ROUTES.PROFILE}
                className="px-4 py-2 text-sm font-medium text-white hover:text-accent transition-colors"
              >
                <span>{user?.username}</span>
                {balance !== null && (
                  <span className="ml-2 text-xs text-accent">
                    {formatPrice(balance)}
                  </span>
                )}
              </Link>
            ) : (
              <button
                onClick={handleLoginClick}
                className="px-6 py-2 text-sm font-medium text-white border border-transparent rounded-button hover:text-accent transition-colors"
              >
                登录
              </button>
            )}

            {/* 注册按钮 */}
            {!isAuthenticated && (
              <Link
                to={ROUTES.REGISTER}
                className="px-6 py-2 text-sm font-medium text-primary bg-accent rounded-button hover:bg-accent-light transition-colors"
              >
                注册
              </Link>
            )}
          </div>

          {/* 移动端菜单按钮 */}
          <button
            className="md:hidden p-2 text-white"
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
        <div className="md:hidden bg-primary border-t border-gray-800">
          <div className="px-4 py-4 space-y-3">
            {/* 导航链接 */}
            {navItems.map((item) => (
              <button
                key={item.path}
                onClick={() => handleNavClick(item.path)}
                className={`block w-full text-left px-3 py-2 text-base font-medium rounded-button transition-colors ${
                  location.pathname === item.path
                    ? 'text-accent bg-gray-900'
                    : 'text-white hover:text-accent hover:bg-gray-900'
                }`}
              >
                {item.label}
              </button>
            ))}

            {/* 购物车 */}
            <button
              onClick={handleCartClick}
              className="flex items-center justify-between w-full px-3 py-2 text-base font-medium text-white rounded-button hover:text-accent hover:bg-gray-900 transition-colors"
            >
              <span>购物车</span>
              <div className="flex items-center space-x-2">
                {totalQuantity > 0 && (
                  <span className="text-accent">{totalQuantity} 件</span>
                )}
                {totalPrice > 0 && (
                  <span className="text-accent">{formatPrice(totalPrice)}</span>
                )}
              </div>
            </button>

            {/* 登录/用户信息 */}
            {isAuthenticated ? (
              <button
                onClick={() => handleNavClick(ROUTES.PROFILE)}
                className="block w-full text-left px-3 py-2 text-base font-medium text-white rounded-button hover:text-accent hover:bg-gray-900 transition-colors"
              >
                <span>{user?.username}</span>
                {balance !== null && (
                  <span className="ml-2 text-xs text-accent">
                    余额：{formatPrice(balance)}
                  </span>
                )}
              </button>
            ) : (
              <button
                onClick={() => handleNavClick(ROUTES.LOGIN)}
                className="block w-full text-left px-3 py-2 text-base font-medium text-white rounded-button hover:text-accent hover:bg-gray-900 transition-colors"
              >
                登录
              </button>
            )}

            {/* 注册 */}
            {!isAuthenticated && (
              <button
                onClick={() => handleNavClick(ROUTES.REGISTER)}
                className="w-full px-6 py-3 text-base font-medium text-primary bg-accent rounded-button hover:bg-accent-light transition-colors"
              >
                注册
              </button>
            )}
          </div>
        </div>
      )}
    </header>
  );
};

export default Header;
