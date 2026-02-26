/**
 * 咖啡卡片组件
 */

import React from 'react';
import { Link } from 'react-router-dom';
import { Coffee } from '../types';
import { formatPrice } from '../utils/helpers';

interface CoffeeCardProps {
  coffee: Coffee;
  onAddToCart?: (coffee: Coffee) => void;
}

const CoffeeCard: React.FC<CoffeeCardProps> = ({ coffee, onAddToCart }) => {
  const handleAddToCart = (e: React.MouseEvent) => {
    e.preventDefault();
    if (onAddToCart) {
      onAddToCart(coffee);
    }
  };

  return (
    <Link to={`/coffee/${coffee.id}`} className="block">
      <div className="card-base group cursor-pointer">
        {/* 图片容器 */}
        <div className="relative overflow-hidden rounded-card mb-4 aspect-square bg-gray-100">
          <img
            src={coffee.imageUrl}
            alt={coffee.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            loading="lazy"
          />
          {/* 库存状态 */}
          {coffee.stock < 10 && coffee.stock > 0 && (
            <div className="absolute top-3 right-3 bg-orange-500 text-white text-xs px-2 py-1 rounded-button">
              仅剩 {coffee.stock} 杯
            </div>
          )}
          {coffee.stock === 0 && (
            <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
              <span className="text-white font-medium">暂时缺货</span>
            </div>
          )}
        </div>

        {/* 内容 */}
        <div className="space-y-2">
          {/* 分类标签 */}
          <div className="text-xs text-accent font-medium uppercase tracking-wide">
            {coffee.category}
          </div>

          {/* 名称 */}
          <h3 className="font-georgia font-bold text-lg text-primary group-hover:text-accent transition-colors line-clamp-1">
            {coffee.name}
          </h3>

          {/* 描述 */}
          <p className="text-sm text-text-secondary line-clamp-2">
            {coffee.description}
          </p>

          {/* 评分 */}
          {coffee.rating && (
            <div className="flex items-center space-x-1">
              <svg className="w-4 h-4 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
              <span className="text-sm font-medium">{coffee.rating}</span>
            </div>
          )}

          {/* 底部信息 */}
          <div className="flex items-center justify-between pt-2 border-t border-gray-100">
            {/* 价格 */}
            <div className="font-bold text-xl text-primary">
              {formatPrice(coffee.price)}
            </div>

            {/* 添加购物车按钮 */}
            <button
              onClick={handleAddToCart}
              disabled={coffee.stock === 0}
              className={`px-4 py-2 rounded-button text-sm font-medium transition-all ${
                coffee.stock === 0
                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  : 'bg-accent text-white hover:bg-accent-light active:scale-95'
              }`}
            >
              {coffee.stock === 0 ? '缺货' : '加入购物车'}
            </button>
          </div>
        </div>
      </div>
    </Link>
  );
};

export default CoffeeCard;
