/**
 * 咖啡列表页面
 */

import React, { useState, useEffect } from 'react';
import { Header, Footer, CoffeeCard, Loading } from '../components';
import { useCart } from '../contexts';
import { coffeeApi } from '../services/api';
import type { Coffee, ApiResponse, PageResponse } from '../types';
import { COFFEE_CATEGORIES } from '../utils/constants';

const CoffeeList: React.FC = () => {
  const { addItem } = useCart();
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [coffees, setCoffees] = useState<Coffee[]>([]);
  const [categories, setCategories] = useState<Array<{ value: string; label: string }>>([
    { value: 'all', label: '全部' },
  ]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const pageSize = 12;

  // 获取分类列表
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await coffeeApi.getCategories() as unknown as ApiResponse<Array<{ code: string; name: string; count: number }>>;
        if (response.code === 200 && response.data) {
          const categoryList = [
            { value: 'all', label: '全部' },
            ...response.data.map((cat) => ({ value: cat.code, label: cat.name })),
          ];
          setCategories(categoryList);
        }
      } catch (error) {
        console.error('获取分类失败:', error);
        // 失败时使用默认分类
        setCategories(COFFEE_CATEGORIES as unknown as Array<{ value: string; label: string }>);
      }
    };

    fetchCategories();
  }, []);

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

  // 本地搜索过滤（在已获取的数据中搜索）
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
    setPage(1); // 切换分类时重置页码
  };

  // 处理搜索
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchKeyword(e.target.value);
  };

  // 处理添加到购物车
  const handleAddToCart = async (coffee: Coffee) => {
    console.log('=== [CoffeeList] 开始添加到购物车 ===');
    console.log('[CoffeeList] coffee:', coffee.name, 'coffeeId:', coffee.coffeeId);

    // 用原生 DOM 弹框测试
    console.log('[CoffeeList] 创建原生 DOM 弹窗...');
    const div = document.createElement('div');
    div.id = 'coffeelist-toast';
    div.style.cssText = 'position:fixed;top:20%;left:50%;transform:translate(-50%,-50%);z-index:999999;background:rgba(0,0,0,0.8);color:#fff;padding:20px 40px;border-radius:12px;font-size:16px;';
    div.textContent = `已将 ${coffee.name} 加入购物车！`;
    document.body.appendChild(div);
    console.log('[CoffeeList] 弹窗已添加到 body');

    setTimeout(() => {
      console.log('[CoffeeList] 移除弹窗');
      div.remove();
    }, 2000);

    try {
      console.log('[CoffeeList] 调用 addItem...');
      await addItem(coffee.coffeeId, 1);
      console.log('[CoffeeList] addItem 完成');
    } catch (err) {
      console.error('[CoffeeList] addItem 出错:', err);
    }
    console.log('=== [CoffeeList] 添加到购物车结束 ===');
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
                {categories.map((category) => (
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
            {isLoading ? (
              <div className="flex justify-center py-16">
                <Loading />
              </div>
            ) : (
              <>
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
                        key={coffee.coffeeId}
                        coffee={coffee}
                        onAddToCart={handleAddToCart}
                      />
                    ))}
                  </div>
                )}

                {/* 分页 */}
                {total > pageSize && (
                  <div className="flex justify-center mt-8 gap-2">
                    <button
                      onClick={() => setPage((p) => Math.max(1, p - 1))}
                      disabled={page === 1}
                      className="px-4 py-2 border border-gray-300 rounded-button text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                    >
                      上一页
                    </button>
                    <span className="px-4 py-2 text-sm text-text-secondary">
                      第 {page} 页 / 共 {Math.ceil(total / pageSize)} 页
                    </span>
                    <button
                      onClick={() => setPage((p) => p + 1)}
                      disabled={page >= Math.ceil(total / pageSize)}
                      className="px-4 py-2 border border-gray-300 rounded-button text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                    >
                      下一页
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default CoffeeList;
