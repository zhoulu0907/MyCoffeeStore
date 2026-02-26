/**
 * 订单页面
 */

import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Header, Footer } from '../components';
import { useAuth, useCart } from '../contexts';
import { ORDER_STATUS, ORDER_TYPES, ROUTES } from '../utils/constants';
import { formatPrice, formatDate } from '../utils/helpers';
import { Order, OrderStatus } from '../types';

// 模拟订单数据
const MOCK_ORDERS: Order[] = [
  {
    id: 1,
    userId: 1,
    orderNo: 'ORD202502260001',
    totalPrice: 88,
    status: 'preparing',
    orderType: 'takeout',
    items: [
      {
        id: 1,
        orderId: 1,
        coffeeId: 1,
        coffeeName: '经典拿铁',
        coffeeImage: 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=200',
        quantity: 2,
        price: 30,
        size: 'M',
      },
      {
        id: 2,
        orderId: 1,
        coffeeId: 3,
        coffeeName: '美式咖啡',
        coffeeImage: 'https://images.unsplash.com/photo-1517701604599-bb29b5c7fa69?w=200',
        quantity: 1,
        price: 18,
        size: 'L',
      },
    ],
    createTime: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
    remark: '少糖，少冰',
  },
  {
    id: 2,
    userId: 1,
    orderNo: 'ORD202502250001',
    totalPrice: 56,
    status: 'completed',
    orderType: 'dine_in',
    items: [
      {
        id: 3,
        orderId: 2,
        coffeeId: 2,
        coffeeName: '卡布奇诺',
        coffeeImage: 'https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=200',
        quantity: 2,
        price: 26,
        size: 'M',
      },
    ],
    createTime: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
  },
  {
    id: 3,
    userId: 1,
    orderNo: 'ORD202502240001',
    totalPrice: 32,
    status: 'cancelled',
    orderType: 'delivery',
    items: [
      {
        id: 4,
        orderId: 3,
        coffeeId: 5,
        coffeeName: '焦糖摩卡',
        coffeeImage: 'https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?w=200',
        quantity: 1,
        price: 32,
        size: 'L',
      },
    ],
    createTime: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(),
  },
];

const Order: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const { clearCart } = useCart();

  const [selectedStatus, setSelectedStatus] = useState<string>('all');
  const [orders] = useState<Order[]>(MOCK_ORDERS);

  // 未登录状态
  React.useEffect(() => {
    if (!isAuthenticated) {
      navigate(ROUTES.LOGIN);
    }
  }, [isAuthenticated, navigate]);

  // 处理返回
  const handleBack = () => {
    navigate(-1);
  };

  // 过滤订单
  const filteredOrders = orders.filter((order) => {
    if (selectedStatus === 'all') return true;
    return order.status === selectedStatus;
  });

  // 取消订单
  const handleCancelOrder = (orderId: number) => {
    if (confirm('确定要取消这个订单吗？')) {
      // 这里应该调用 API 取消订单
      console.log('取消订单:', orderId);
    }
  };

  // 确认收货
  const handleConfirmOrder = (orderId: number) => {
    if (confirm('确认已收到咖啡？')) {
      // 这里应该调用 API 确认收货
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
                onClick={() => setSelectedStatus('all')}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                  selectedStatus === 'all'
                    ? 'bg-primary text-white'
                    : 'bg-gray-100 text-text-primary hover:bg-gray-200'
                }`}
              >
                全部
              </button>
              <button
                onClick={() => setSelectedStatus('pending')}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                  selectedStatus === 'pending'
                    ? 'bg-primary text-white'
                    : 'bg-gray-100 text-text-primary hover:bg-gray-200'
                }`}
              >
                待确认
              </button>
              <button
                onClick={() => setSelectedStatus('preparing')}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                  selectedStatus === 'preparing'
                    ? 'bg-primary text-white'
                    : 'bg-gray-100 text-text-primary hover:bg-gray-200'
                }`}
              >
                制作中
              </button>
              <button
                onClick={() => setSelectedStatus('completed')}
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
            {filteredOrders.length === 0 ? (
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
                {filteredOrders.map((order) => (
                  <OrderCard
                    key={order.id}
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
  order: Order;
  onCancel: (orderId: number) => void;
  onConfirm: (orderId: number) => void;
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
              src={item.coffeeImage}
              alt={item.coffeeName}
              className="w-16 h-16 object-cover rounded"
            />
            <div>
              <div className="font-medium text-primary">{item.coffeeName}</div>
              <div className="text-sm text-text-secondary">
                {item.size && `(${item.size})`} × {item.quantity}
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
                onClick={() => onCancel(order.id)}
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
              onClick={() => onConfirm(order.id)}
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
