/**
 * 咖啡列表页面
 */

import React, { useState } from 'react';
import { Header, Footer, CoffeeCard, Loading } from '../components';
import { useCart } from '../contexts';
import { MOCK_COFFEES, COFFEE_CATEGORIES } from '../utils/constants';
import { Coffee, CoffeeCategory } from '../types';

const CoffeeList: React.FC = () => {
  const { addItem } = useCart();
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [searchKeyword, setSearchKeyword] = useState('');

  // 过滤咖啡列表
  const filteredCoffees = MOCK_COFFEES.filter((coffee) => {
    const matchCategory = selectedCategory === 'all' || coffee.category === selectedCategory;
    const matchSearch =
      !searchKeyword ||
      coffee.name.toLowerCase().includes(searchKeyword.toLowerCase()) ||
      coffee.description.toLowerCase().includes(searchKeyword.toLowerCase());
    return matchCategory && matchSearch;
  });

  // 处理分类选择
  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category);
  };

  // 处理搜索
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchKeyword(e.target.value);
  };

  // 处理添加到购物车
  const handleAddToCart = (coffee: Coffee) => {
    addItem(coffee, 1);
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 bg-white">
        {/* 页头 */}
        <div className="bg-primary text-white py-12">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <h1 className="font-georgia text-4xl font-bold mb-4">咖啡菜单</h1>
            <p className="text-gray-300 text-lg">
              精选来自世界各地的优质咖啡，为您带来独一无二的味觉体验
            </p>
          </div>
        </div>

        {/* 搜索和过滤区域 */}
        <div className="bg-gray-50 py-6 border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
              {/* 分类过滤 */}
              <div className="flex flex-wrap gap-2">
                {COFFEE_CATEGORIES.map((category) => (
                  <button
                    key={category.value}
                    onClick={() => handleCategoryChange(category.value)}
                    className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                      selectedCategory === category.value
                        ? 'bg-primary text-white'
                        : 'bg-white text-text-primary hover:bg-gray-100'
                    }`}
                  >
                    {category.label}
                  </button>
                ))}
              </div>

              {/* 搜索框 */}
              <div className="relative">
                <input
                  type="text"
                  value={searchKeyword}
                  onChange={handleSearchChange}
                  placeholder="搜索咖啡..."
                  className="w-full md:w-64 pl-10 pr-4 py-2 border border-gray-200 rounded-button focus:outline-none focus:ring-2 focus:ring-accent focus:border-transparent"
                />
                <svg
                  className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
              </div>
            </div>
          </div>
        </div>

        {/* 咖啡列表 */}
        <div className="py-12">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            {/* 结果数量 */}
            <div className="mb-6">
              <p className="text-text-secondary">
                找到 <span className="font-medium text-primary">{filteredCoffees.length}</span> 款咖啡
              </p>
            </div>

            {/* 空状态 */}
            {filteredCoffees.length === 0 ? (
              <div className="text-center py-16">
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
                    d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                <h3 className="text-lg font-medium text-primary mb-2">未找到相关咖啡</h3>
                <p className="text-text-secondary">
                  试试调整筛选条件或搜索关键词
                </p>
              </div>
            ) : (
              /* 咖啡卡片网格 */
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {filteredCoffees.map((coffee) => (
                  <CoffeeCard
                    key={coffee.id}
                    coffee={coffee}
                    onAddToCart={handleAddToCart}
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

export default CoffeeList;
