/**
 * 首页
 */

import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Header, Footer, CoffeeCard, Carousel } from '../components';
import { useCart } from '../contexts';
import { coffeeApi } from '../services/api';
import { CAROUSEL_ITEMS } from '../utils/constants';
import type { Coffee, ApiResponse, PageResponse } from '../types';

const Home: React.FC = () => {
  const { addItem } = useCart();
  const [featuredCoffees, setFeaturedCoffees] = useState<Coffee[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // 获取精选咖啡（取前3个）
  useEffect(() => {
    const fetchFeaturedCoffees = async () => {
      try {
        const response = await coffeeApi.getList({ page: 1, size: 3 }) as unknown as ApiResponse<PageResponse<Coffee>>;

        if (response.code === 200 && response.data) {
          setFeaturedCoffees(response.data.list || []);
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

  // 处理添加到购物车
  const handleAddToCart = (coffee: Coffee) => {
    addItem(coffee.coffeeId, 1);
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1">
        {/* Hero 轮播图区域 */}
        <section className="h-[600px]">
          <Carousel items={CAROUSEL_ITEMS} />
        </section>

        {/* 精选咖啡区域 */}
        <section className="section-padding bg-white">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            {/* 标题 */}
            <div className="text-center mb-12">
              <h2 className="font-georgia text-4xl font-bold text-primary mb-4">
                精选咖啡
              </h2>
              <p className="text-text-secondary text-lg">
                用心烘焙，每一杯都是艺术品
              </p>
            </div>

            {/* 咖啡卡片网格 */}
            {isLoading ? (
              <div className="text-center py-12 text-text-secondary">加载中...</div>
            ) : featuredCoffees.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                {featuredCoffees.map((coffee) => (
                  <CoffeeCard
                    key={coffee.coffeeId}
                    coffee={coffee}
                    onAddToCart={handleAddToCart}
                  />
                ))}
              </div>
            ) : (
              <div className="text-center py-12 text-text-secondary">暂无咖啡数据</div>
            )}

            {/* 查看更多按钮 */}
            <div className="text-center mt-12">
              <Link
                to="/coffee"
                className="inline-block px-8 py-3 bg-primary text-white rounded-button font-medium hover:bg-gray-800 transition-colors"
              >
                查看全部咖啡
              </Link>
            </div>
          </div>
        </section>

        {/* 关于我们区域 */}
        <section className="section-padding bg-primary">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
              {/* 内容 */}
              <div>
                <h2 className="font-georgia text-3xl md:text-4xl font-bold text-white mb-6">
                  关于 MyCoffeeStore
                </h2>
                <p className="text-gray-300 text-lg leading-relaxed mb-8">
                  位于旧金山 Haight Ashbury 的精品咖啡店，我们致力于为每一位顾客提供最优质的咖啡体验。
                  从精选咖啡豆到专业烘焙，从精心冲煮到完美呈现，每一杯咖啡都承载着我们的热情与专注。
                </p>

                {/* 店铺信息 */}
                <div className="space-y-4">
                  <div className="flex items-center text-gray-300">
                    <svg
                      className="w-6 h-6 mr-4 text-accent"
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
                    <span>周一至周日 7:00 - 22:00</span>
                  </div>
                  <div className="flex items-center text-gray-300">
                    <svg
                      className="w-6 h-6 mr-4 text-accent"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                      />
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                    </svg>
                    <span>123 Haight St, San Francisco, CA 94117</span>
                  </div>
                  <div className="flex items-center text-gray-300">
                    <svg
                      className="w-6 h-6 mr-4 text-accent"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"
                      />
                    </svg>
                    <span>(415) 555-0123</span>
                  </div>
                </div>
              </div>

              {/* 图片 */}
              <div className="relative">
                <div className="aspect-[4/3] rounded-2xl overflow-hidden">
                  <img
                    src="https://images.unsplash.com/photo-1736813133887-321f44e44224?w=800"
                    alt="咖啡店环境"
                    className="w-full h-full object-cover"
                  />
                </div>
                {/* 装饰元素 */}
                <div className="absolute -bottom-6 -left-6 w-24 h-24 bg-accent rounded-2xl -z-10" />
                <div className="absolute -top-6 -right-6 w-24 h-24 bg-accent rounded-2xl -z-10" />
              </div>
            </div>
          </div>
        </section>

        {/* 特色服务区域 */}
        <section className="section-padding bg-gray-50">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-12">
              <h2 className="font-georgia text-3xl md:text-4xl font-bold text-primary mb-4">
                为什么选择我们
              </h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              {/* 特色 1 */}
              <div className="text-center p-6">
                <div className="w-16 h-16 bg-accent rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg
                    className="w-8 h-8 text-white"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"
                    />
                  </svg>
                </div>
                <h3 className="font-georgia text-xl font-bold text-primary mb-2">
                  精选咖啡豆
                </h3>
                <p className="text-text-secondary">
                  从世界各地精心挑选优质咖啡豆，确保每一杯都拥有最纯正的风味
                </p>
              </div>

              {/* 特色 2 */}
              <div className="text-center p-6">
                <div className="w-16 h-16 bg-accent rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg
                    className="w-8 h-8 text-white"
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
                </div>
                <h3 className="font-georgia text-xl font-bold text-primary mb-2">
                  现磨现做
                </h3>
                <p className="text-text-secondary">
                  每一杯咖啡都是现磨现做，保留最新鲜的口感和香气
                </p>
              </div>

              {/* 特色 3 */}
              <div className="text-center p-6">
                <div className="w-16 h-16 bg-accent rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg
                    className="w-8 h-8 text-white"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                </div>
                <h3 className="font-georgia text-xl font-bold text-primary mb-2">
                  专业服务
                </h3>
                <p className="text-text-secondary">
                  经验丰富的咖啡师团队，为您打造完美的咖啡体验
                </p>
              </div>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
};

export default Home;
