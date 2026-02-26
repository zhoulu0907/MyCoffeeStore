/**
 * 结算页面
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Header, Footer, Loading } from '../components';
import { useAuth, useCart } from '../contexts';
import { orderApi, userApi } from '../services/api';
import { ORDER_TYPES, ROUTES } from '../utils/constants';
import { formatPrice } from '../utils/helpers';
import type { ApiResponse } from '../types';

const Checkout: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const { items, totalQuantity, totalPrice, clearCart } = useCart();

  // 订单类型状态
  const [orderType, setOrderType] = useState<string>('dine_in');

  // 配送地址状态
  const [address, setAddress] = useState({
    address: '',
    city: '',
    state: '',
    zipCode: '',
    phone: '',
  });

  // 备注状态
  const [remark, setRemark] = useState('');

  // 余额状态
  const [balance, setBalance] = useState<number | null>(null);
  const [balanceLoading, setBalanceLoading] = useState(true);

  // 提交状态
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  // 未登录则跳转到登录页
  useEffect(() => {
    if (!isAuthenticated) {
      navigate(ROUTES.LOGIN);
    }
  }, [isAuthenticated, navigate]);

  // 加载余额数据
  useEffect(() => {
    const fetchBalance = async () => {
      if (!isAuthenticated) return;

      setBalanceLoading(true);
      try {
        const response = await userApi.getBalance() as unknown as ApiResponse<{ balance: number }>;

        if (response.code === 200 && response.data) {
          setBalance(response.data.balance);
        }
      } catch (error) {
        console.error('获取余额失败:', error);
        setBalance(null);
      } finally {
        setBalanceLoading(false);
      }
    };

    fetchBalance();
  }, [isAuthenticated]);

  // 处理返回
  const handleBack = () => {
    navigate(-1);
  };

  // 余额是否充足
  const isBalanceSufficient = balance !== null && balance >= totalPrice;

  // 支付后剩余余额
  const remainingBalance = balance !== null ? balance - totalPrice : 0;

  // 外卖地址是否填写完整
  const isAddressComplete =
    orderType !== 'delivery' ||
    (address.address.trim() !== '' &&
      address.city.trim() !== '' &&
      address.state.trim() !== '' &&
      address.zipCode.trim() !== '' &&
      address.phone.trim() !== '');

  // 是否可以提交
  const canSubmit = isBalanceSufficient && isAddressComplete && !submitting && items.length > 0;

  // 处理确认支付
  const handleSubmit = async () => {
    if (!canSubmit) return;

    setSubmitting(true);
    setError('');

    try {
      const response = await orderApi.create({
        items: items.map(item => ({
          coffeeId: item.coffeeId,
          quantity: item.quantity,
          price: item.price,
        })),
        orderType: orderType,
        remark: remark || undefined,
        deliveryAddress: orderType === 'delivery' ? {
          address: address.address,
          city: address.city,
          state: address.state,
          zipCode: address.zipCode,
          phone: address.phone,
        } : undefined,
      }) as unknown as ApiResponse;

      if (response.code === 200) {
        // 成功：清空购物车，跳转到订单页面
        clearCart();
        navigate(ROUTES.ORDER);
        alert('订单创建成功！');
      } else {
        setError(response.message || '创建订单失败，请稍后重试');
      }
    } catch (error) {
      console.error('创建订单失败:', error);
      setError('创建订单失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  // 未登录
  if (!isAuthenticated) {
    return null;
  }

  // 购物车为空
  if (items.length === 0) {
    return (
      <div className="min-h-screen flex flex-col bg-gray-50">
        <Header />

        <main className="flex-1 flex items-center justify-center px-4">
          <div className="text-center max-w-md">
            <svg
              className="mx-auto h-32 w-32 text-gray-300 mb-6"
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
            <h2 className="text-2xl font-bold text-primary mb-2">购物车是空的</h2>
            <p className="text-text-secondary mb-6">
              请先添加商品到购物车再进行结算
            </p>
            <Link
              to={ROUTES.COFFEE_LIST}
              className="btn-primary"
            >
              浏览咖啡
            </Link>
          </div>
        </main>

        <Footer />
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Header />

      <main className="flex-1 py-12">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
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
            <h1 className="text-xl font-bold">确认订单</h1>
          </div>

          <div className="bg-white rounded-b-2xl shadow-sm">
            {/* 订单预览区域 */}
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-lg font-bold text-primary mb-4">订单商品</h2>
              <div className="space-y-3">
                {items.map((item) => (
                  <div
                    key={item.cartId}
                    className="flex items-center justify-between py-3 border-b border-gray-100 last:border-b-0"
                  >
                    <div className="flex items-center gap-3 flex-1 min-w-0">
                      <img
                        src={item.imageUrl}
                        alt={item.coffeeName}
                        className="w-12 h-12 object-cover rounded-button flex-shrink-0"
                      />
                      <div className="min-w-0">
                        <div className="font-medium text-primary truncate">
                          {item.coffeeName}
                        </div>
                        <div className="text-sm text-text-secondary">
                          {formatPrice(item.price)} x {item.quantity}
                        </div>
                      </div>
                    </div>
                    <div className="text-right font-bold text-primary ml-4">
                      {formatPrice(item.subtotal)}
                    </div>
                  </div>
                ))}
              </div>

              {/* 总价 */}
              <div className="flex justify-between items-center mt-4 pt-4 border-t border-gray-200">
                <span className="text-text-secondary">
                  共 {totalQuantity} 件商品
                </span>
                <div className="text-xl font-bold text-primary">
                  合计：{formatPrice(totalPrice)}
                </div>
              </div>
            </div>

            {/* 订单类型选择 */}
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-lg font-bold text-primary mb-4">订单类型</h2>
              <div className="flex gap-3">
                {ORDER_TYPES.map((type) => (
                  <button
                    key={type.value}
                    onClick={() => setOrderType(type.value)}
                    className={`flex-1 px-4 py-3 rounded-button text-sm font-medium transition-all ${
                      orderType === type.value
                        ? 'bg-primary text-white'
                        : 'bg-gray-100 text-text-primary hover:bg-gray-200'
                    }`}
                  >
                    {type.label}
                  </button>
                ))}
              </div>
            </div>

            {/* 配送地址表单（仅外卖时显示） */}
            {orderType === 'delivery' && (
              <div className="p-6 border-b border-gray-200">
                <h2 className="text-lg font-bold text-primary mb-4">配送地址</h2>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-text-secondary mb-1">
                      详细地址 <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={address.address}
                      onChange={(e) => setAddress({ ...address, address: e.target.value })}
                      placeholder="请输入详细地址"
                      className="input-base w-full"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-text-secondary mb-1">
                        城市 <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={address.city}
                        onChange={(e) => setAddress({ ...address, city: e.target.value })}
                        placeholder="请输入城市"
                        className="input-base w-full"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-text-secondary mb-1">
                        省份 <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={address.state}
                        onChange={(e) => setAddress({ ...address, state: e.target.value })}
                        placeholder="请输入省份"
                        className="input-base w-full"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-text-secondary mb-1">
                        邮编 <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={address.zipCode}
                        onChange={(e) => setAddress({ ...address, zipCode: e.target.value })}
                        placeholder="请输入邮编"
                        className="input-base w-full"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-text-secondary mb-1">
                        联系电话 <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={address.phone}
                        onChange={(e) => setAddress({ ...address, phone: e.target.value })}
                        placeholder="请输入联系电话"
                        className="input-base w-full"
                      />
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* 备注输入框 */}
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-lg font-bold text-primary mb-4">订单备注</h2>
              <textarea
                value={remark}
                onChange={(e) => setRemark(e.target.value)}
                placeholder="如有特殊需求，请在此备注（可选）"
                rows={3}
                className="input-base w-full resize-none"
              />
            </div>

            {/* 账户余额展示 */}
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-lg font-bold text-primary mb-4">支付信息</h2>

              {balanceLoading ? (
                <div className="flex justify-center py-4">
                  <Loading />
                </div>
              ) : (
                <div className="space-y-3">
                  <div className="flex justify-between text-text-secondary">
                    <span>账户余额</span>
                    <span className="font-medium text-primary">
                      {balance !== null ? formatPrice(balance) : '获取失败'}
                    </span>
                  </div>
                  <div className="flex justify-between text-text-secondary">
                    <span>本次消费</span>
                    <span className="font-medium text-accent">
                      -{formatPrice(totalPrice)}
                    </span>
                  </div>
                  <div className="flex justify-between pt-3 border-t border-gray-200">
                    <span className="font-medium text-text-secondary">支付后余额</span>
                    <span className={`font-bold ${remainingBalance >= 0 ? 'text-primary' : 'text-red-500'}`}>
                      {balance !== null ? formatPrice(remainingBalance) : '--'}
                    </span>
                  </div>

                  {/* 余额不足警告 */}
                  {balance !== null && !isBalanceSufficient && (
                    <div className="mt-3 p-3 bg-red-50 border border-red-200 rounded-button">
                      <p className="text-red-600 text-sm font-medium">
                        余额不足！当前余额 {formatPrice(balance)}，还需充值 {formatPrice(totalPrice - balance)}
                      </p>
                    </div>
                  )}

                  {/* 余额获取失败提示 */}
                  {balance === null && !balanceLoading && (
                    <div className="mt-3 p-3 bg-yellow-50 border border-yellow-200 rounded-button">
                      <p className="text-yellow-600 text-sm font-medium">
                        无法获取账户余额，请刷新页面重试
                      </p>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* 错误信息 */}
            {error && (
              <div className="px-6 pt-4">
                <div className="p-3 bg-red-50 border border-red-200 rounded-button">
                  <p className="text-red-600 text-sm font-medium">{error}</p>
                </div>
              </div>
            )}

            {/* 底部操作区 */}
            <div className="p-6">
              {/* 确认支付按钮 */}
              <button
                onClick={handleSubmit}
                disabled={!canSubmit}
                className={`w-full py-4 text-lg font-medium rounded-button transition-all ${
                  canSubmit
                    ? 'btn-primary'
                    : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                }`}
              >
                {submitting ? '提交中...' : `确认支付 ${formatPrice(totalPrice)}`}
              </button>

              {/* 返回购物车链接 */}
              <div className="text-center mt-4">
                <Link
                  to={ROUTES.CART}
                  className="text-accent hover:text-accent-light font-medium"
                >
                  返回购物车
                </Link>
              </div>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default Checkout;
