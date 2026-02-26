/**
 * 购物车页面
 */

import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Header, Footer } from '../components';
import { useCart } from '../contexts';
import { formatPrice } from '../utils/helpers';
import { ROUTES } from '../utils/constants';
import type { CartItem } from '../types';

const Cart: React.FC = () => {
  const navigate = useNavigate();
  const { items, totalQuantity, totalPrice, removeItem, updateQuantity, clearCart } = useCart();

  // 处理返回
  const handleBack = () => {
    navigate(-1);
  };

  // 处理删除商品
  const handleRemoveItem = (cartId: number) => {
    if (confirm('确定要删除这个商品吗？')) {
      removeItem(cartId);
    }
  };

  // 处理清空购物车
  const handleClearCart = () => {
    if (confirm('确定要清空购物车吗？')) {
      clearCart();
    }
  };

  // 处理去结算
  const handleCheckout = () => {
    navigate(ROUTES.CHECKOUT);
  };

  // 空状态
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
              快去挑选您喜欢的咖啡吧！
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
            <h1 className="text-xl font-bold">购物车 ({totalQuantity} 件)</h1>
          </div>

          {/* 购物车内容 */}
          <div className="bg-white rounded-b-2xl shadow-sm">
            {/* 商品列表 */}
            <div className="divide-y divide-gray-100">
              {items.map((item) => (
                <CartItemCard
                  key={item.cartId}
                  item={item}
                  onUpdateQuantity={updateQuantity}
                  onRemove={handleRemoveItem}
                />
              ))}
            </div>

            {/* 底部汇总 */}
            <div className="border-t border-gray-200 p-6">
              {/* 清空购物车按钮 */}
              <div className="flex justify-end mb-4">
                <button
                  onClick={handleClearCart}
                  className="text-sm text-text-secondary hover:text-red-500 transition-colors"
                >
                  清空购物车
                </button>
              </div>

              {/* 汇总信息 */}
              <div className="space-y-3 mb-6">
                <div className="flex justify-between text-text-secondary">
                  <span>商品数量</span>
                  <span>{totalQuantity} 件</span>
                </div>
                <div className="flex justify-between text-text-secondary">
                  <span>运费</span>
                  <span className="text-accent">免费</span>
                </div>
                <div className="flex justify-between text-xl font-bold text-primary pt-3 border-t border-gray-200">
                  <span>合计</span>
                  <span>{formatPrice(totalPrice)}</span>
                </div>
              </div>

              {/* 结算按钮 */}
              <button
                onClick={handleCheckout}
                className="w-full btn-primary py-4 text-lg"
              >
                去结算
              </button>

              {/* 继续购物链接 */}
              <div className="text-center mt-4">
                <Link
                  to={ROUTES.COFFEE_LIST}
                  className="text-accent hover:text-accent-light font-medium"
                >
                  继续购物
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

/**
 * 购物车项目卡片组件
 */
interface CartItemCardProps {
  item: CartItem;
  onUpdateQuantity: (cartId: number, quantity: number) => void;
  onRemove: (cartId: number) => void;
}

const CartItemCard: React.FC<CartItemCardProps> = ({ item, onUpdateQuantity, onRemove }) => {
  const handleQuantityChange = (delta: number) => {
    const newQuantity = item.quantity + delta;
    if (newQuantity >= 1) {
      onUpdateQuantity(item.cartId, newQuantity);
    }
  };

  return (
    <div className="p-6 hover:bg-gray-50 transition-colors">
      <div className="flex gap-4">
        {/* 商品图片 */}
        <Link to={`/coffee/${item.coffeeId}`} className="flex-shrink-0">
          <img
            src={item.imageUrl}
            alt={item.coffeeName}
            className="w-24 h-24 object-cover rounded-button"
          />
        </Link>

        {/* 商品信息 */}
        <div className="flex-1 min-w-0">
          <div className="flex justify-between mb-2">
            <Link
              to={`/coffee/${item.coffeeId}`}
              className="font-georgia font-bold text-lg text-primary hover:text-accent transition-colors line-clamp-1"
            >
              {item.coffeeName}
            </Link>
            <button
              onClick={() => onRemove(item.cartId)}
              className="text-text-secondary hover:text-red-500 transition-colors"
              aria-label="删除"
            >
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                />
              </svg>
            </button>
          </div>

          {/* 库存状态 */}
          {item.stock < 10 && item.stock > 0 && (
            <span className="inline-block px-2 py-1 bg-orange-50 text-orange-600 text-xs rounded mr-2">
              仅剩 {item.stock} 件
            </span>
          )}

          {/* 数量和价格 */}
          <div className="flex items-center justify-between mt-3">
            {/* 数量控制器 */}
            <div className="flex items-center border border-gray-300 rounded-button">
              <button
                onClick={() => handleQuantityChange(-1)}
                disabled={item.quantity <= 1}
                className="w-8 h-8 flex items-center justify-center hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <svg
                  className="w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
                </svg>
              </button>
              <input
                type="number"
                value={item.quantity}
                onChange={(e) => {
                  const val = parseInt(e.target.value);
                  if (val >= 1) {
                    onUpdateQuantity(item.cartId, val);
                  }
                }}
                min="1"
                className="w-14 text-center border-x border-gray-300 py-2 focus:outline-none"
              />
              <button
                onClick={() => handleQuantityChange(1)}
                disabled={item.quantity >= item.stock}
                className="w-8 h-8 flex items-center justify-center hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <svg
                  className="w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
              </button>
            </div>

            {/* 单价和总价 */}
            <div className="text-right">
              <div className="text-sm text-text-secondary">
                {formatPrice(item.price)} x {item.quantity}
              </div>
              <div className="text-lg font-bold text-primary">
                {formatPrice(item.subtotal)}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cart;
