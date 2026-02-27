/**
 * 咖啡详情页面
 */

import React, { useState, useEffect } from 'react';
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
  const { addItem } = useCart();

  const [selectedSize, setSelectedSize] = useState<string>('M');
  const [quantity, setQuantity] = useState<number>(1);
  const [coffee, setCoffee] = useState<Coffee | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isAdding, setIsAdding] = useState(false);
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
      <div className="min-h-screen flex flex-col" style={{ backgroundColor: '#F7F1E8' }}>
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
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#F7F1E8' }}>
        <div className="text-center">
          <h1 className="text-4xl font-bold text-primary mb-4">404</h1>
          <p className="text-text-secondary mb-6">{error || '未找到该咖啡'}</p>
          <Link
            to={ROUTES.COFFEE_LIST}
            className="inline-block px-6 py-3 bg-primary text-background rounded-button"
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

  // 处理添加到购物车
  const handleAddToCart = async () => {
    setIsAdding(true);
    try {
      await addItem(coffee.coffeeId, quantity);
    } catch (err) {
      console.error('添加购物车失败:', err);
    } finally {
      setIsAdding(false);
    }
  };

  // 处理立即购买
  const handleBuyNow = () => {
    handleAddToCart();
    navigate(ROUTES.CART);
  };

  return (
    <div className="min-h-screen flex flex-col" style={{ backgroundColor: '#F7F1E8' }}>
      <Header />

      <main className="flex-1">
        {/* 面包屑导航 */}
        <div className="px-6 py-4">
          <p className="text-xs text-text-light">
            首页 &gt; 咖啡列表 &gt; {coffee.name}
          </p>
        </div>

        {/* 详情内容 */}
        <div className="px-6 pb-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {/* 左侧：图片 */}
            <div className="rounded-2xl overflow-hidden">
              <img
                src={coffee.imageUrl || 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=800'}
                alt={coffee.name}
                className="w-full h-full object-cover"
              />
            </div>

            {/* 右侧：信息 */}
            <div className="flex flex-col gap-3">
              {/* 分类标签 */}
              <p className="text-xs text-text-light">
                分类：{coffee.category || '美式'}
              </p>

              {/* 名称 */}
              <h1 className="text-[34px] font-bold text-primary leading-tight">
                {coffee.name}
              </h1>

              {/* 评分 */}
              <p className="text-sm text-text-secondary">
                评分 {coffee.rating || 4.8}
              </p>

              {/* 描述 */}
              <p className="text-base text-text-secondary leading-relaxed">
                {coffee.description}
              </p>

              {/* 冲泡时间 */}
              <p className="text-xs text-text-light">
                冲泡时间：{coffee.brewingTime || '2-3'} 分钟
              </p>

              {/* 尺寸选择 - 优化触摸目标尺寸为 44px */}
              {coffee.size && coffee.size.length > 0 && (
                <div className="flex gap-2 h-11">
                  {COFFEE_SIZES.map((size) => {
                    const isAvailable = coffee.size!.includes(size.value);
                    return (
                      <button
                        key={size.value}
                        onClick={() => handleSizeChange(size.value)}
                        disabled={!isAvailable}
                        className={`flex-1 rounded-lg text-sm font-medium transition-all ${
                          !isAvailable
                            ? 'text-text-muted cursor-not-allowed'
                            : selectedSize === size.value
                            ? 'bg-primary text-background'
                            : 'text-primary hover:opacity-80'
                        }`}
                        style={{
                          backgroundColor: selectedSize === size.value ? '#2A1A15' : '#DCCCB9',
                        }}
                      >
                        {size.label}
                      </button>
                    );
                  })}
                </div>
              )}

              {/* 数量选择 - 优化触摸目标尺寸为 44px */}
              <div className="flex items-center gap-2 h-11">
                <button
                  onClick={() => handleQuantityChange(-1)}
                  disabled={quantity <= 1}
                  className="w-11 h-full rounded-lg text-text-secondary hover:opacity-80 disabled:opacity-30 disabled:cursor-not-allowed flex items-center justify-center"
                  style={{ backgroundColor: '#DCCCB9' }}
                >
                  −
                </button>
                <span className="flex-1 text-center font-medium text-primary">
                  {quantity}
                </span>
                <button
                  onClick={() => handleQuantityChange(1)}
                  disabled={quantity >= coffee.stock}
                  className="w-11 h-full rounded-lg text-text-secondary hover:opacity-80 disabled:opacity-30 disabled:cursor-not-allowed flex items-center justify-center"
                  style={{ backgroundColor: '#DCCCB9' }}
                >
                  +
                </button>
              </div>

              {/* 价格 */}
              <p className="text-2xl font-bold text-primary leading-tight">
                单价 {formatPrice(finalPrice)} · 总价 {formatPrice(finalPrice * quantity)}
              </p>

              {/* 按钮组 */}
              <div className="flex gap-2.5 h-12">
                <button
                  onClick={handleAddToCart}
                  disabled={coffee.stock === 0 || isAdding}
                  className="flex-1 rounded-xl text-sm font-bold text-background hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
                  style={{ backgroundColor: '#2A1A15' }}
                >
                  加入购物车
                </button>
                <button
                  onClick={handleBuyNow}
                  disabled={coffee.stock === 0}
                  className="flex-1 rounded-xl text-sm font-bold text-text-secondary hover:opacity-80 disabled:opacity-50 disabled:cursor-not-allowed border border-text-secondary"
                >
                  立即购买
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default CoffeeDetail;
