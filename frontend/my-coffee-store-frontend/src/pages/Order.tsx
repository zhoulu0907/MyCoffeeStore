/**
 * 订单页面
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Header, Footer, Loading } from '../components';
import { useAuth, useCart } from '../contexts';
import { orderApi } from '../services/api';
import { ORDER_STATUS, ORDER_TYPES, ROUTES } from '../utils/constants';
import { formatPrice, formatDate } from '../utils/helpers';
import type { Order as OrderType, OrderStatus, ApiResponse, PageResponse } from '../types';

const Order: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const { clearCart } = useCart();

  const [selectedStatus, setSelectedStatus] = useState<string>('all');
  const [orders, setOrders] = useState<OrderType[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const pageSize = 10;

  // 未登录状态
  React.useEffect(() => {
    if (!isAuthenticated) {
      navigate(ROUTES.LOGIN);
    }
  }, [isAuthenticated, navigate]);

  // 获取订单列表
  useEffect(() => {
    const fetchOrders = async () => {
      if (!isAuthenticated) return;

      setIsLoading(true);
      try {
        const params: { status?: string; page: number; size: number } = {
          page,
          size: pageSize,
        };

        // 状态筛选
        if (selectedStatus !== 'all') {
          params.status = selectedStatus;
        }

        const response = await orderApi.getList(params) as unknown as ApiResponse<PageResponse<OrderType>>;

        if (response.code === 200 && response.data) {
          setOrders(response.data.list || []);
          setTotal(response.data.total || 0);
        }
      } catch (error) {
        console.error('获取订单列表失败:', error);
        setOrders([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrders();
  }, [isAuthenticated, selectedStatus, page]);

  // 处理返回
  const handleBack = () => {
    navigate(-1);
  };

  // 处理状态筛选切换
  const handleStatusChange = (status: string) => {
    setSelectedStatus(status);
    setPage(1); // 切换状态时重置页码
  };

  // 取消订单
  const handleCancelOrder = async (orderId: string) => {
    if (confirm('确定要取消这个订单吗？')) {
      try {
        const response = await orderApi.cancel(orderId, '用户主动取消') as unknown as ApiResponse;

        if (response.code === 200) {
          // 取消成功，刷新订单列表
          setOrders((prevOrders) =>
            prevOrders.map((order) =>
              order.orderNo === orderId ? { ...order, status: 'cancelled' as OrderStatus } : order
            )
          );
        } else {
          alert(response.message || '取消订单失败');
        }
      } catch (error) {
        console.error('取消订单失败:', error);
        alert('取消订单失败，请稍后重试');
      }
    }
  };

  // 确认收货
  const handleConfirmOrder = (orderId: string) => {
    if (confirm('确认已收到咖啡？')) {
      // 确认收货逻辑（如果后端有此接口可添加）
      console.log('确认收货:', orderId);
    }
  };

  if (!isAuthenticated) {
    return null;
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
            <h1 className="text-xl font-bold">我的订单</h1>
          </div>

          {/* 订单筛选 */}
          <div className="bg-white border-b border-gray-200 px-6 py-4">
            <div className="flex flex-wrap gap-2">
              <button
                onClick={() => handleStatusChange('all')}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                  selectedStatus === 'all'
                    ? 'bg-primary text-white'
                    : 'bg-gray-100 text-text-primary hover:bg-gray-200'
                }`}
              >
                全部
              </button>
              <button
                onClick={() => handleStatusChange('pending')}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                  selectedStatus === 'pending'
                    ? 'bg-primary text-white'
                    : 'bg-gray-100 text-text-primary hover:bg-gray-200'
                }`}
              >
                待确认
              </button>
              <button
                onClick={() => handleStatusChange('preparing')}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                  selectedStatus === 'preparing'
                    ? 'bg-primary text-white'
                    : 'bg-gray-100 text-text-primary hover:bg-gray-200'
                }`}
              >
                制作中
              </button>
              <button
                onClick={() => handleStatusChange('completed')}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                  selectedStatus === 'completed'
                    ? 'bg-primary text-white'
                    : 'bg-gray-100 text-text-primary hover:bg-gray-200'
                }`}
              >
                已完成
              </button>
            </div>
          </div>

          {/* 订单列表 */}
          <div className="bg-white rounded-b-2xl shadow-sm">
            {isLoading ? (
              <div className="flex justify-center py-16">
                <Loading />
              </div>
            ) : orders.length === 0 ? (
              /* 空状态 */
              <div className="py-16 text-center">
                <svg
                  className="mx-auto h-24 w-24 text-gray-300 mb-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                  />
                </svg>
                <h3 className="text-lg font-medium text-primary mb-2">暂无订单</h3>
                <p className="text-text-secondary mb-6">
                  快去点一杯您喜欢的咖啡吧！
                </p>
                <Link
                  to={ROUTES.COFFEE_LIST}
                  className="btn-primary"
                >
                  浏览咖啡
                </Link>
              </div>
            ) : (
              /* 订单卡片列表 */
              <div className="divide-y divide-gray-100">
                {orders.map((order) => (
                  <OrderCard
                    key={order.orderNo}
                    order={order}
                    onCancel={handleCancelOrder}
                    onConfirm={handleConfirmOrder}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

/**
 * 订单卡片组件
 */
interface OrderCardProps {
  order: OrderType;
  onCancel: (orderId: string) => void;
  onConfirm: (orderId: string) => void;
}

const OrderCard: React.FC<OrderCardProps> = ({ order, onCancel, onConfirm }) => {
  const statusInfo = ORDER_STATUS[order.status];
  const orderTypeInfo = ORDER_TYPES.find((t) => t.value === order.orderType);

  return (
    <div className="p-6 hover:bg-gray-50 transition-colors">
      {/* 订单头部 */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-4">
          <span className="font-mono text-sm text-text-secondary">
            {order.orderNo}
          </span>
          <span className="px-2 py-1 bg-gray-100 text-text-secondary text-xs rounded">
            {orderTypeInfo?.label}
          </span>
        </div>
        <span
          className={`px-3 py-1 text-sm font-medium rounded-full ${
            statusInfo.color
          } ${statusInfo.bgColor}`}
        >
          {statusInfo.label}
        </span>
      </div>

      {/* 订单商品 */}
      <div className="flex gap-4 mb-4 overflow-x-auto">
        {order.items.map((item) => (
          <div
            key={item.id}
            className="flex-shrink-0 flex items-center gap-3 p-3 bg-gray-50 rounded-button"
          >
            <img
              src={item.coffeeImage || item.imageUrl || ''}
              alt={item.coffeeName}
              className="w-16 h-16 object-cover rounded"
            />
            <div>
              <div className="font-medium text-primary">{item.coffeeName}</div>
              <div className="text-sm text-text-secondary">
                {item.size && `(${item.size})`} x {item.quantity}
              </div>
              <div className="text-sm font-medium text-accent">
                {formatPrice(item.price * item.quantity)}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* 订单信息 */}
      <div className="text-sm text-text-secondary mb-4">
        <div className="flex justify-between">
          <span>下单时间：</span>
          <span>{formatDate(order.createTime)}</span>
        </div>
        {order.remark && (
          <div className="flex justify-between mt-1">
            <span>备注：</span>
            <span>{order.remark}</span>
          </div>
        )}
      </div>

      {/* 订单底部 */}
      <div className="flex items-center justify-between pt-4 border-t border-gray-200">
        <div className="text-lg font-bold text-primary">
          总计：{formatPrice(order.totalPrice)}
        </div>

        {/* 操作按钮 */}
        <div className="flex gap-2">
          {order.status === 'pending' && (
            <>
              <button
                onClick={() => onCancel(order.orderNo)}
                className="px-4 py-2 border border-red-500 text-red-500 rounded-button text-sm font-medium hover:bg-red-50 transition-colors"
              >
                取消订单
              </button>
            </>
          )}
          {order.status === 'preparing' && (
            <button
              className="px-4 py-2 border border-gray-300 text-text-primary rounded-button text-sm font-medium hover:bg-gray-50 transition-colors"
              disabled
            >
              制作中...
            </button>
          )}
          {order.status === 'ready' && (
            <button
              onClick={() => onConfirm(order.orderNo)}
              className="px-4 py-2 bg-accent text-white rounded-button text-sm font-medium hover:bg-accent-light transition-colors"
            >
              确认收货
            </button>
          )}
          {order.status === 'completed' && (
            <Link
              to="/coffee"
              className="px-4 py-2 border border-gray-300 text-text-primary rounded-button text-sm font-medium hover:bg-gray-50 transition-colors"
            >
              再次购买
            </Link>
          )}
        </div>
      </div>
    </div>
  );
};

export default Order;
