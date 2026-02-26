/**
 * 账户页面
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Header, Footer, Loading } from '../components';
import { useAuth } from '../contexts';
import { userApi } from '../services/api';
import { ROUTES } from '../utils/constants';
import { formatPrice, formatDate } from '../utils/helpers';
import type { ApiResponse } from '../types';

const Profile: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();

  const [balance, setBalance] = useState<number | null>(null);
  const [isLoadingBalance, setIsLoadingBalance] = useState(true);
  const [rechargeAmount, setRechargeAmount] = useState('');
  const [isRecharging, setIsRecharging] = useState(false);
  const [showRecharge, setShowRecharge] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // 未登录跳转
  useEffect(() => {
    if (!isAuthenticated) {
      navigate(ROUTES.LOGIN);
    }
  }, [isAuthenticated, navigate]);

  // 获取余额
  useEffect(() => {
    const fetchBalance = async () => {
      if (!isAuthenticated) return;

      try {
        const response = await userApi.getBalance() as unknown as ApiResponse<{ balance: number }>;
        if (response.code === 200 && response.data) {
          setBalance(response.data.balance);
        }
      } catch (error) {
        console.error('获取余额失败:', error);
      } finally {
        setIsLoadingBalance(false);
      }
    };

    fetchBalance();
  }, [isAuthenticated]);

  // 处理充值
  const handleRecharge = async () => {
    const amount = parseFloat(rechargeAmount);
    if (isNaN(amount) || amount <= 0) {
      setMessage({ type: 'error', text: '请输入有效的充值金额' });
      return;
    }

    setIsRecharging(true);
    setMessage(null);

    try {
      const response = await userApi.recharge(amount) as unknown as ApiResponse<{ balance: number }>;
      if (response.code === 200 && response.data) {
        setBalance(response.data.balance);
        setRechargeAmount('');
        setShowRecharge(false);
        setMessage({ type: 'success', text: `充值成功！已充值 ${formatPrice(amount)}` });
      } else {
        setMessage({ type: 'error', text: response.message || '充值失败' });
      }
    } catch (error: any) {
      setMessage({ type: 'error', text: error?.response?.data?.message || '充值失败，请稍后重试' });
    } finally {
      setIsRecharging(false);
    }
  };

  // 处理退出登录
  const handleLogout = async () => {
    await logout();
    navigate(ROUTES.HOME);
  };

  // 处理返回
  const handleBack = () => {
    navigate(-1);
  };

  // 快捷充值金额
  const quickAmounts = [50, 100, 200, 500];

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Header />

      <main className="flex-1 py-12">
        <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* 页头 */}
          <div className="bg-primary text-white px-6 py-4 rounded-t-2xl flex items-center">
            <button
              onClick={handleBack}
              className="mr-4 text-white hover:text-accent transition-colors"
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
                  d="M15 19l-7-7 7-7"
                />
              </svg>
            </button>
            <h1 className="text-xl font-bold">我的账户</h1>
          </div>

          <div className="bg-white rounded-b-2xl shadow-sm divide-y divide-gray-100">
            {/* 用户信息 */}
            <div className="p-6">
              <div className="flex items-center gap-4">
                <div className="w-16 h-16 bg-accent rounded-full flex items-center justify-center text-white text-2xl font-bold">
                  {user?.username?.charAt(0).toUpperCase()}
                </div>
                <div>
                  <h2 className="text-xl font-bold text-primary">{user?.username}</h2>
                  <p className="text-sm text-text-secondary">{user?.email}</p>
                  {user?.phone && (
                    <p className="text-sm text-text-secondary">{user.phone}</p>
                  )}
                </div>
              </div>
            </div>

            {/* 账户余额 */}
            <div className="p-6">
              <h3 className="text-sm font-medium text-text-secondary mb-3">账户余额</h3>
              {isLoadingBalance ? (
                <div className="flex justify-center py-4">
                  <Loading />
                </div>
              ) : (
                <div>
                  <div className="flex items-end justify-between mb-4">
                    <div className="text-3xl font-bold text-primary">
                      {balance !== null ? formatPrice(balance) : '--'}
                    </div>
                    <button
                      onClick={() => setShowRecharge(!showRecharge)}
                      className="px-4 py-2 bg-accent text-white text-sm font-medium rounded-button hover:bg-accent-light transition-colors"
                    >
                      {showRecharge ? '取消' : '充值'}
                    </button>
                  </div>

                  {/* 充值面板 */}
                  {showRecharge && (
                    <div className="bg-gray-50 rounded-xl p-4">
                      {/* 快捷金额 */}
                      <div className="grid grid-cols-4 gap-2 mb-3">
                        {quickAmounts.map((amount) => (
                          <button
                            key={amount}
                            onClick={() => setRechargeAmount(String(amount))}
                            className={`py-2 text-sm font-medium rounded-button transition-colors ${
                              rechargeAmount === String(amount)
                                ? 'bg-primary text-white'
                                : 'bg-white text-primary border border-gray-200 hover:border-primary'
                            }`}
                          >
                            {formatPrice(amount)}
                          </button>
                        ))}
                      </div>

                      {/* 自定义金额 */}
                      <div className="flex gap-2">
                        <input
                          type="number"
                          value={rechargeAmount}
                          onChange={(e) => setRechargeAmount(e.target.value)}
                          placeholder="输入充值金额"
                          className="input-base flex-1"
                          min="1"
                          step="0.01"
                          disabled={isRecharging}
                        />
                        <button
                          onClick={handleRecharge}
                          disabled={isRecharging || !rechargeAmount}
                          className="px-6 py-2 btn-primary disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
                        >
                          {isRecharging ? '充值中...' : '确认充值'}
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* 提示消息 */}
            {message && (
              <div className="px-6 pt-4">
                <div
                  className={`px-4 py-3 rounded-button text-sm ${
                    message.type === 'success'
                      ? 'bg-green-50 border border-green-200 text-green-600'
                      : 'bg-red-50 border border-red-200 text-red-600'
                  }`}
                >
                  {message.text}
                </div>
              </div>
            )}

            {/* 功能入口 */}
            <div className="p-6 space-y-3">
              <h3 className="text-sm font-medium text-text-secondary mb-3">常用功能</h3>

              <Link
                to={ROUTES.ORDER}
                className="flex items-center justify-between p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <svg className="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                  </svg>
                  <span className="font-medium text-primary">我的订单</span>
                </div>
                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>

              <Link
                to={ROUTES.COFFEE_LIST}
                className="flex items-center justify-between p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <svg className="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
                  </svg>
                  <span className="font-medium text-primary">浏览菜单</span>
                </div>
                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>

              <Link
                to={ROUTES.CART}
                className="flex items-center justify-between p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <svg className="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                  <span className="font-medium text-primary">购物车</span>
                </div>
                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Link>
            </div>

            {/* 退出登录 */}
            <div className="p-6">
              <button
                onClick={handleLogout}
                className="w-full py-3 text-red-500 font-medium text-center rounded-button border border-red-200 hover:bg-red-50 transition-colors"
              >
                退出登录
              </button>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default Profile;
