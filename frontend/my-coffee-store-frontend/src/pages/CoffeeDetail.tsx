/**
 * 咖啡详情页面
 */

import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Header, Footer, Loading } from '../components';
import { useCart } from '../contexts';
import { coffeeApi } from '../services/api';
import { COFFEE_SIZES, ROUTES } from '../utils/constants';
import { formatPrice } from '../utils/helpers';
import type { Coffee, ApiResponse } from '../types';

const CoffeeDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { addItem, isInCart } = useCart();

  const [selectedSize, setSelectedSize] = useState<string>('M');
  const [quantity, setQuantity] = useState<number>(1);
  const [coffee, setCoffee] = useState<Coffee | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isAdding, setIsAdding] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // 从后端获取咖啡详情
  useEffect(() => {
    const fetchCoffeeDetail = async () => {
      if (!id) return;

      setIsLoading(true);
      setError(null);
      try {
        const coffeeId = parseInt(id);
        const response = await coffeeApi.getDetail(coffeeId) as unknown as ApiResponse<Coffee>;

        if (response.code === 200 && response.data) {
          setCoffee(response.data);
        } else {
          setError(response.message || '获取咖啡详情失败');
        }
      } catch (err) {
        console.error('获取咖啡详情失败:', err);
        setError('获取咖啡详情失败');
      } finally {
        setIsLoading(false);
      }
    };

    fetchCoffeeDetail();
  }, [id]);

  // 加载中状态
  if (isLoading) {
    return (
      <div className="min-h-screen flex flex-col">
        <Header />
        <main className="flex-1 flex items-center justify-center">
          <Loading />
        </main>
        <Footer />
      </div>
    );
  }

  // 如果咖啡不存在，显示404
  if (!coffee || error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-primary mb-4">404</h1>
          <p className="text-text-secondary mb-6">{error || '未找到该咖啡'}</p>
          <Link
            to={ROUTES.COFFEE_LIST}
            className="btn-primary"
          >
            返回咖啡列表
          </Link>
        </div>
      </div>
    );
  }

  // 计算价格（包含尺寸加价）
  const sizePrice = COFFEE_SIZES.find((s) => s.value === selectedSize)?.price || 0;
  const finalPrice = coffee.price + sizePrice;

  // 处理尺寸选择
  const handleSizeChange = (size: string) => {
    if (coffee.size && coffee.size.includes(size)) {
      setSelectedSize(size);
    }
  };

  // 处理数量变化
  const handleQuantityChange = (delta: number) => {
    const newQuantity = quantity + delta;
    if (newQuantity >= 1 && newQuantity <= coffee.stock) {
      setQuantity(newQuantity);
    }
  };

  // 显示 toast 提示
  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 5000);
  };

  // 处理添加到购物车
  const handleAddToCart = async () => {
    console.log('=== [CoffeeDetail] 开始添加到购物车 ===');
    console.log('[CoffeeDetail] coffeeId:', coffee!.coffeeId);
    console.log('[CoffeeDetail] quantity:', quantity);
    console.log('[CoffeeDetail] selectedSize:', selectedSize);

    // 用原生 DOM 弹框测试，完全绕过 React
    console.log('[CoffeeDetail] 创建原生 DOM 弹窗...');
    const div = document.createElement('div');
    div.id = 'debug-toast';
    div.style.cssText = 'position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);z-index:999999;background:rgba(0,0,0,0.8);color:#fff;padding:24px 48px;border-radius:12px;font-size:18px;';
    div.textContent = '添加购物车成功！';
    console.log('[CoffeeDetail] 准备将弹窗添加到 body, 当前 body 子元素数量:', document.body.children.length);
    document.body.appendChild(div);
    console.log('[CoffeeDetail] 弹窗已添加, 新的 body 子元素数量:', document.body.children.length);
    console.log('[CoffeeDetail] 弹窗元素:', div);
    setTimeout(() => {
      console.log('[CoffeeDetail] 移除弹窗');
      div.remove();
    }, 2000);

    setIsAdding(true);
    try {
      console.log('[CoffeeDetail] 调用 CartContext.addItem...');
      await addItem(coffee!.coffeeId, quantity);
      console.log('[CoffeeDetail] addItem 调用完成');
    } catch (err) {
      console.error('[CoffeeDetail] addItem 出错:', err);
    } finally {
      setIsAdding(false);
      console.log('=== [CoffeeDetail] 添加到购物车流程结束 ===');
    }
  };

  // 处理立即购买
  const handleBuyNow = () => {
    handleAddToCart();
    navigate(ROUTES.CART);
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 bg-white py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* 面包屑导航 */}
          <nav className="mb-8 text-sm">
            <ol className="flex items-center space-x-2">
              <li>
                <Link
                  to={ROUTES.HOME}
                  className="text-text-secondary hover:text-accent"
                >
                  首页
                </Link>
              </li>
              <li className="text-text-secondary">/</li>
              <li>
                <Link
                  to={ROUTES.COFFEE_LIST}
                  className="text-text-secondary hover:text-accent"
                >
                  咖啡列表
                </Link>
              </li>
              <li className="text-text-secondary">/</li>
              <li className="text-primary font-medium">{coffee.name}</li>
            </ol>
          </nav>

          {/* 详情内容 */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
            {/* 左侧：图片 */}
            <div>
              <div className="relative aspect-square rounded-2xl overflow-hidden bg-gray-100 mb-4">
                <img
                  src={coffee.imageUrl}
                  alt={coffee.name}
                  className="w-full h-full object-cover"
                />
                {coffee.stock < 10 && coffee.stock > 0 && (
                  <div className="absolute top-4 right-4 bg-orange-500 text-white px-3 py-1 rounded-button text-sm font-medium">
                    仅剩 {coffee.stock} 杯
                  </div>
                )}
              </div>
            </div>

            {/* 右侧：信息 */}
            <div>
              {/* 分类标签 */}
              <div className="text-accent font-medium text-sm uppercase tracking-wide mb-2">
                {coffee.category}
              </div>

              {/* 名称 */}
              <h1 className="font-georgia text-4xl font-bold text-primary mb-4">
                {coffee.name}
              </h1>

              {/* 评分 */}
              {coffee.rating && (
                <div className="flex items-center space-x-2 mb-6">
                  <div className="flex items-center">
                    {[...Array(5)].map((_, i) => (
                      <svg
                        key={i}
                        className={`w-5 h-5 ${
                          i < Math.floor(coffee.rating!)
                            ? 'text-yellow-400'
                            : 'text-gray-300'
                        }`}
                        fill="currentColor"
                        viewBox="0 0 20 20"
                      >
                        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                      </svg>
                    ))}
                  </div>
                  <span className="text-sm text-text-secondary">
                    {coffee.rating} 评分
                  </span>
                </div>
              )}

              {/* 描述 */}
              <p className="text-text-secondary text-lg leading-relaxed mb-6">
                {coffee.description}
              </p>

              {/* 冲泡时间 */}
              {coffee.brewingTime && (
                <div className="flex items-center text-text-secondary mb-6">
                  <svg
                    className="w-5 h-5 mr-2"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  <span>冲泡时间：约 {coffee.brewingTime} 分钟</span>
                </div>
              )}

              {/* 尺寸选择 */}
              {coffee.size && coffee.size.length > 0 && (
                <div className="mb-6">
                  <h3 className="font-medium text-primary mb-3">选择尺寸</h3>
                  <div className="flex flex-wrap gap-3">
                    {COFFEE_SIZES.map((size) => {
                      const isAvailable = coffee.size!.includes(size.value);
                      return (
                        <button
                          key={size.value}
                          onClick={() => handleSizeChange(size.value)}
                          disabled={!isAvailable}
                          className={`px-6 py-3 rounded-button border-2 transition-all ${
                            !isAvailable
                              ? 'border-gray-200 text-gray-400 cursor-not-allowed'
                              : selectedSize === size.value
                              ? 'border-accent bg-accent text-white'
                              : 'border-gray-300 text-primary hover:border-accent'
                          }`}
                        >
                          <div className="text-center">
                            <div className="font-medium">{size.label}</div>
                            {size.price > 0 && (
                              <div className="text-xs opacity-75">
                                +{formatPrice(size.price)}
                              </div>
                            )}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* 数量选择 */}
              <div className="mb-6">
                <h3 className="font-medium text-primary mb-3">选择数量</h3>
                <div className="flex items-center space-x-3">
                  <button
                    onClick={() => handleQuantityChange(-1)}
                    disabled={quantity <= 1}
                    className="w-10 h-10 flex items-center justify-center border border-gray-300 rounded-button hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <svg
                      className="w-4 h-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M20 12H4"
                      />
                    </svg>
                  </button>
                  <input
                    type="number"
                    value={quantity}
                    onChange={(e) => {
                      const val = parseInt(e.target.value);
                      if (val >= 1 && val <= coffee.stock) {
                        setQuantity(val);
                      }
                    }}
                    min="1"
                    max={coffee.stock}
                    className="w-20 text-center border border-gray-300 rounded-button py-2 focus:outline-none focus:ring-2 focus:ring-accent"
                  />
                  <button
                    onClick={() => handleQuantityChange(1)}
                    disabled={quantity >= coffee.stock}
                    className="w-10 h-10 flex items-center justify-center border border-gray-300 rounded-button hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <svg
                      className="w-4 h-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 4v16m8-8H4"
                      />
                    </svg>
                  </button>
                  <span className="text-sm text-text-secondary">
                    库存：{coffee.stock} 杯
                  </span>
                </div>
              </div>

              {/* 价格和操作 */}
              <div className="border-t border-gray-200 pt-6">
                <div className="flex items-center justify-between mb-6">
                  <div>
                    <div className="text-sm text-text-secondary mb-1">总价</div>
                    <div className="text-3xl font-bold text-primary">
                      {formatPrice(finalPrice * quantity)}
                    </div>
                  </div>
                  <div className="text-sm text-text-secondary">
                    单价：{formatPrice(finalPrice)}
                  </div>
                </div>

                {/* 按钮组 */}
                <div className="flex flex-col sm:flex-row gap-3">
                  <button
                    onClick={handleAddToCart}
                    disabled={coffee.stock === 0 || isAdding}
                    className="flex-1 btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <svg
                      className="w-5 h-5 inline mr-2"
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
                    {isAdding ? '添加中...' : '加入购物车'}
                  </button>
                  <button
                    onClick={handleBuyNow}
                    disabled={coffee.stock === 0}
                    className="flex-1 btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    立即购买
                  </button>
                </div>

                {/* 已在购物车提示 */}
                {isInCart(coffee.coffeeId) && (
                  <p className="mt-3 text-sm text-accent flex items-center">
                    <svg
                      className="w-4 h-4 mr-1"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                    >
                      <path
                        fillRule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                        clipRule="evenodd"
                      />
                    </svg>
                    已添加到购物车
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>
      </main>

      <Footer />

      {/* Toast 提示 */}
      {toast && createPortal(
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, zIndex: 99999, display: 'flex', alignItems: 'center', justifyContent: 'center', pointerEvents: 'none' }}>
          <div style={{ background: 'rgba(0,0,0,0.75)', color: '#ffffff', padding: '20px 40px', borderRadius: '12px', fontSize: '16px', fontWeight: 'bold' }}>
            {toast}
          </div>
        </div>,
        document.body
      )}
    </div>
  );
};

export default CoffeeDetail;
