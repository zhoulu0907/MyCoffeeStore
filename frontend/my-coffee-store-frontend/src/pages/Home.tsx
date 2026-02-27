/**
 * 首页 - Haight Ashbury Cafe
 */

import React, { useState, useEffect } from 'react';
import { Header } from '../components';
import { useCart } from '../contexts';
import { coffeeApi } from '../services/api';
import { formatPrice } from '../utils/helpers';
import type { Coffee, ApiResponse, PageResponse } from '../types';

interface FeaturedCoffee extends Coffee {
  recommendationReason?: string;
  rating?: number;
}

const Home: React.FC = () => {
  const { addItem } = useCart();
  const [featuredCoffees, setFeaturedCoffees] = useState<FeaturedCoffee[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Hero 三栏轮播数据
  const heroItems = [
    {
      title: 'Haight House Blend',
      subtitle: '坚果与可可尾韵',
      image: 'https://images.unsplash.com/photo-1762657433581-15773c721a42?w=800',
    },
    {
      title: 'Golden Gate Cold Brew',
      subtitle: '12h 冷萃，清爽明亮',
      image: 'https://images.unsplash.com/photo-1762545387133-2eb76f3de1f3?w=800',
    },
    {
      title: 'Sunset Pour Over',
      subtitle: '花香柑橘，层次细腻',
      image: 'https://images.unsplash.com/photo-1729277133093-5915b9b18c9c?w=800',
    },
  ];

  useEffect(() => {
    const fetchFeaturedCoffees = async () => {
      try {
        const response = await coffeeApi.getList({ page: 1, size: 3 }) as unknown as ApiResponse<PageResponse<Coffee>>;
        if (response.code === 200 && response.data) {
          const coffees = (response.data.list || []).map((coffee, index) => ({
            ...coffee,
            recommendationReason: getRecommendationReason(index),
            rating: getRating(index),
          }));
          setFeaturedCoffees(coffees);
        }
      } catch (error) {
        console.error('获取精选咖啡失败:', error);
        setFeaturedCoffees([]);
      } finally {
        setIsLoading(false);
      }
    };
    fetchFeaturedCoffees();
  }, []);

  const getRecommendationReason = (index: number) => {
    const reasons = [
      '推荐理由：偏好中深烘、早晨常点意式',
      '推荐理由：近期更偏爱果香与手冲层次',
      '推荐理由：当前气温偏低，冷萃口感更顺滑',
    ];
    return reasons[index] || reasons[0];
  };

  const getRating = (index: number) => {
    const ratings = [5, 4, 3];
    return ratings[index] || 5;
  };

  const renderStars = (rating: number) => {
    return '★'.repeat(rating) + '☆'.repeat(5 - rating);
  };

  const handleAddToCart = async (coffee: Coffee) => {
    try {
      await addItem(coffee.coffeeId, 1);
    } catch (err) {
      console.error('添加购物车失败:', err);
    }
  };

  return (
    <div className="min-h-screen flex flex-col" style={{ backgroundColor: '#F7F1E8' }}>
      <Header />

      <main className="flex-1">
        {/* Hero 三栏轮播区域 */}
        <section className="h-[380px]">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 h-full px-6 py-4">
            {heroItems.map((item, index) => (
              <div
                key={index}
                className="relative rounded-2xl overflow-hidden h-full"
              >
                <img
                  src={item.image}
                  alt={item.title}
                  className="w-full h-full object-cover"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
                <div className="absolute bottom-4 left-4 right-4">
                  <h3 className="text-white text-xl md:text-2xl font-bold mb-1">
                    {item.title}
                  </h3>
                  <p className="text-white/90 text-sm">
                    {item.subtitle}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* 推荐咖啡区域 */}
        <section className="py-3">
          <div className="px-6">
            <h2 className="text-3xl font-bold text-primary mb-3">
              推荐咖啡
            </h2>
            <p className="text-text-muted text-sm mb-3">
              本周人气推荐三款：
            </p>

            {isLoading ? (
              <div className="text-center py-12 text-text-light">加载中...</div>
            ) : featuredCoffees.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {featuredCoffees.map((coffee) => (
                  <div
                    key={coffee.coffeeId}
                    className="bg-white rounded-xl p-3 shadow-sm flex flex-col"
                  >
                    <img
                      src={coffee.imageUrl || 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=400'}
                      alt={coffee.name}
                      className="w-full h-[180px] object-cover rounded-lg mb-3"
                    />
                    <h3 className="text-xl font-bold text-primary mb-1">
                      {coffee.name}
                    </h3>
                    <p className="text-text-secondary mb-2">
                      {formatPrice(coffee.price)}
                    </p>
                    <p className="text-xs text-text-light mb-2">
                      {coffee.recommendationReason}
                    </p>
                    <p className="text-xs text-gold-dark mb-3">
                      推荐指数：{renderStars(coffee.rating || 5)}
                    </p>
                    <button
                      onClick={() => handleAddToCart(coffee)}
                      className="mt-auto w-full py-3 bg-primary text-background rounded-xl font-bold text-sm hover:opacity-90 transition-opacity"
                    >
                      加入购物车
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-12 text-text-light">暂无咖啡数据</div>
            )}
          </div>
        </section>

        {/* 关于我们区域 */}
        <section style={{ backgroundColor: '#F1E7DB' }} className="py-4">
          <div className="px-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div className="flex flex-col justify-center">
                <h2 className="text-3xl font-bold text-primary mb-2.5">
                  关于我们
                </h2>
                <p className="text-text-secondary leading-relaxed">
                  位于 San Francisco Haight Ashbury，我们专注单一产地与创意拼配。
                  联系电话：+1 (415) 555-0136
                  地址：1452 Haight St, San Francisco, CA
                </p>
              </div>
              <div className="rounded-2xl overflow-hidden">
                <img
                  src="https://images.unsplash.com/photo-1769987030211-4630ace160e2?w=600"
                  alt="咖啡店环境"
                  className="w-full h-full object-cover"
                />
              </div>
            </div>
          </div>
        </section>

        {/* 特色服务区域 */}
        <section className="py-3">
          <div className="px-6">
            <h2 className="text-3xl font-bold text-primary mb-3">
              特色服务
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="rounded-xl p-4" style={{ backgroundColor: '#F1E7DB' }}>
                <h3 className="text-xl font-bold text-primary mb-2">
                  精选咖啡豆
                </h3>
                <p className="text-text-secondary text-sm leading-relaxed">
                  每周小批量烘焙，追求稳定风味。
                </p>
              </div>
              <div className="rounded-xl p-4" style={{ backgroundColor: '#F1E7DB' }}>
                <h3 className="text-xl font-bold text-primary mb-2">
                  现磨现做
                </h3>
                <p className="text-text-secondary text-sm leading-relaxed">
                  下单后研磨冲煮，保留最佳香气。
                </p>
              </div>
              <div className="rounded-xl p-4" style={{ backgroundColor: '#F1E7DB' }}>
                <h3 className="text-xl font-bold text-primary mb-2">
                  专业服务
                </h3>
                <p className="text-text-secondary text-sm leading-relaxed">
                  咖啡师可根据口味推荐豆单与萃取方案。
                </p>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="py-12 px-6" style={{ backgroundColor: '#1A110E' }}>
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <p className="text-sm" style={{ color: '#F2E8DC' }}>
            Haight Ashbury Cafe · Brew Bold, Stay Local
          </p>
          <p className="text-sm" style={{ color: '#CDBAA4' }}>
            快速链接: 首页 菜单 登录 | © 2026
          </p>
        </div>
      </footer>
    </div>
  );
};

export default Home;
