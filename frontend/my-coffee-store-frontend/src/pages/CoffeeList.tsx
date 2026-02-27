/**
 * 咖啡列表页面 - 推荐咖啡
 */

import React, { useState, useEffect } from 'react';
import { Header, Footer, Loading } from '../components';
import { useCart } from '../contexts';
import { coffeeApi } from '../services/api';
import { formatPrice } from '../utils/helpers';
import type { Coffee, ApiResponse, PageResponse } from '../types';

const CoffeeList: React.FC = () => {
  const { addItem } = useCart();
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [coffees, setCoffees] = useState<Coffee[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const pageSize = 6;

  const categories = [
    { value: 'all', label: '全部' },
    { value: 'espresso', label: '意式' },
    { value: 'hand_drip', label: '手冲' },
    { value: 'cold_brew', label: '冷萃' },
    { value: 'blend', label: '拼配' },
  ];

  // 获取咖啡列表
  useEffect(() => {
    const fetchCoffees = async () => {
      setIsLoading(true);
      try {
        const params: { category?: string; page: number; size: number } = {
          page,
          size: pageSize,
        };

        // 分类筛选
        if (selectedCategory !== 'all') {
          params.category = selectedCategory;
        }

        const response = await coffeeApi.getList(params) as unknown as ApiResponse<PageResponse<Coffee>>;

        if (response.code === 200 && response.data) {
          setCoffees(response.data.list || []);
          setTotal(response.data.total || 0);
        }
      } catch (error) {
        console.error('获取咖啡列表失败:', error);
        setCoffees([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCoffees();
  }, [selectedCategory, page]);

  // 本地搜索过滤
  const filteredCoffees = coffees.filter((coffee) => {
    if (!searchKeyword) return true;
    return (
      coffee.name.toLowerCase().includes(searchKeyword.toLowerCase()) ||
      coffee.description.toLowerCase().includes(searchKeyword.toLowerCase())
    );
  });

  // 处理分类选择
  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category);
    setPage(1);
  };

  // 处理搜索
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchKeyword(e.target.value);
  };

  // 处理添加到购物车
  const handleAddToCart = async (coffee: Coffee) => {
    try {
      await addItem(coffee.coffeeId, 1);
    } catch (err) {
      console.error('添加购物车失败:', err);
    }
  };

  const totalPages = Math.ceil(total / pageSize);

  return (
    <div className="min-h-screen flex flex-col" style={{ backgroundColor: '#F7F1E8' }}>
      <Header />

      <main className="flex-1">
        {/* 页头 */}
        <div className="px-6 py-5">
          <h1 className="text-[34px] font-bold text-primary mb-2">
            推荐咖啡
          </h1>
          <p className="text-text-secondary text-base">
            基于你的口味与时段偏好，为你精选三款。
          </p>
        </div>

        {/* 筛选和搜索区域 */}
        <div className="px-6 mb-3">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 py-3 rounded-2xl" style={{ backgroundColor: '#EFE4D7' }}>
            {/* 分类过滤 - 优化触摸目标尺寸为 44px */}
            <div className="flex flex-wrap gap-2">
              {categories.map((category) => (
                <button
                  key={category.value}
                  onClick={() => handleCategoryChange(category.value)}
                  className={`px-4 h-11 rounded-full text-sm font-medium transition-all ${
                    selectedCategory === category.value
                      ? 'bg-primary text-background'
                      : 'text-primary hover:opacity-80'
                  }`}
                  style={{ backgroundColor: selectedCategory === category.value ? '#2A1A15' : '#DCCCB9' }}
                >
                  {category.label}
                </button>
              ))}
            </div>

            {/* 搜索框 - 优化触摸目标尺寸为 44px */}
            <div className="relative">
              <input
                type="text"
                value={searchKeyword}
                onChange={handleSearchChange}
                placeholder="搜索咖啡、风味或产地"
                className="w-full sm:w-64 h-11 px-4 bg-white rounded-xl text-sm text-text-muted placeholder-text-muted focus:outline-none"
              />
            </div>
          </div>
        </div>

        {/* 咖啡列表 */}
        <div className="px-6 pb-3">
          {isLoading ? (
            <div className="flex justify-center py-16">
              <Loading />
            </div>
          ) : (
            <>
              {/* 空状态 */}
              {filteredCoffees.length === 0 ? (
                <div className="text-center py-16">
                  <h3 className="text-lg font-medium text-primary mb-2">未找到相关咖啡</h3>
                  <p className="text-text-secondary">
                    试试调整筛选条件或搜索关键词
                  </p>
                </div>
              ) : (
                /* 咖啡卡片网格 */
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3.5">
                  {filteredCoffees.map((coffee) => (
                    <div
                      key={coffee.coffeeId}
                      className="bg-white rounded-xl p-2.5 shadow-sm flex flex-col gap-2"
                    >
                      <img
                        src={coffee.imageUrl || 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=400'}
                        alt={coffee.name}
                        className="w-full h-[216px] object-cover rounded-lg"
                      />
                      <h3 className="text-lg font-bold text-primary">
                        {coffee.name}
                      </h3>
                      <p className="text-xs text-text-secondary line-clamp-2">
                        {coffee.description}
                      </p>
                      <div className="flex items-center justify-between mt-auto">
                        <span className="text-base font-semibold text-text-secondary">
                          {formatPrice(coffee.price)}
                        </span>
                        <button
                          onClick={() => handleAddToCart(coffee)}
                          className="px-4 h-11 bg-primary text-background rounded-xl text-sm font-bold hover:opacity-90 transition-opacity"
                        >
                          加入购物车
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* 分页 - 优化触摸目标尺寸为 44px */}
              {total > pageSize && (
                <div className="flex justify-center mt-6 gap-3 py-4">
                  <button
                    onClick={() => setPage((p) => Math.max(1, p - 1))}
                    disabled={page === 1}
                    className="w-28 h-11 rounded-xl text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:opacity-80 transition-opacity"
                    style={{ backgroundColor: page === 1 ? '#DCCCB9' : '#DCCCB9', color: page === 1 ? '#4F372D' : '#4F372D' }}
                  >
                    上一页
                  </button>
                  <button
                    onClick={() => setPage((p) => p + 1)}
                    disabled={page >= totalPages}
                    className="w-28 h-11 rounded-xl text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:opacity-90 transition-opacity"
                    style={{ backgroundColor: page >= totalPages ? '#DCCCB9' : '#2A1A15', color: page >= totalPages ? '#4F372D' : '#F7F1E8' }}
                  >
                    下一页
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default CoffeeList;
