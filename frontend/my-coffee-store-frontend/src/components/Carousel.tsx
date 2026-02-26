/**
 * 轮播图组件
 */

import React, { useState, useEffect } from 'react';

interface CarouselItem {
  id: number;
  image: string;
  title: string;
  subtitle: string;
}

interface CarouselProps {
  items: CarouselItem[];
  autoPlay?: boolean;
  interval?: number;
}

const Carousel: React.FC<CarouselProps> = ({
  items,
  autoPlay = true,
  interval = 5000,
}) => {
  const [currentIndex, setCurrentIndex] = useState(0);

  // 自动播放
  useEffect(() => {
    if (!autoPlay) return;

    const timer = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % items.length);
    }, interval);

    return () => clearInterval(timer);
  }, [autoPlay, interval, items.length]);

  // 切换到指定幻灯片
  const goToSlide = (index: number) => {
    setCurrentIndex(index);
  };

  // 上一张
  const goToPrevious = () => {
    setCurrentIndex((prev) => (prev - 1 + items.length) % items.length);
  };

  // 下一张
  const goToNext = () => {
    setCurrentIndex((prev) => (prev + 1) % items.length);
  };

  if (items.length === 0) {
    return null;
  }

  return (
    <div className="relative w-full h-full overflow-hidden bg-black">
      {/* 幻灯片 */}
      <div
        className="flex transition-transform duration-500 ease-out h-full"
        style={{ transform: `translateX(-${currentIndex * 100}%)` }}
      >
        {items.map((item) => (
          <div
            key={item.id}
            className="min-w-full h-full relative"
          >
            {/* 背景图片 */}
            <img
              src={item.image}
              alt={item.title}
              className="w-full h-full object-cover"
            />

            {/* 遮罩层 */}
            <div className="absolute inset-0 bg-black bg-opacity-40"></div>

            {/* 内容 */}
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="text-center text-white px-4 max-w-4xl">
                <h2 className="font-georgia text-4xl md:text-6xl font-bold mb-4 animate-fadeIn">
                  {item.title}
                </h2>
                <p className="text-lg md:text-xl font-light mb-8 animate-fadeIn">
                  {item.subtitle}
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* 导航按钮 */}
      {items.length > 1 && (
        <>
          {/* 上一张按钮 */}
          <button
            onClick={goToPrevious}
            className="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 flex items-center justify-center bg-white bg-opacity-20 hover:bg-opacity-30 text-white rounded-full transition-all"
            aria-label="上一张"
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

          {/* 下一张按钮 */}
          <button
            onClick={goToNext}
            className="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 flex items-center justify-center bg-white bg-opacity-20 hover:bg-opacity-30 text-white rounded-full transition-all"
            aria-label="下一张"
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
                d="M9 5l7 7-7 7"
              />
            </svg>
          </button>
        </>
      )}

      {/* 指示器 */}
      {items.length > 1 && (
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2 flex space-x-3">
          {items.map((_, index) => (
            <button
              key={index}
              onClick={() => goToSlide(index)}
              className={`w-12 h-1 rounded-full transition-all ${
                index === currentIndex
                  ? 'bg-accent'
                  : 'bg-white bg-opacity-50 hover:bg-opacity-75'
              }`}
              aria-label={`转到第 ${index + 1} 张`}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default Carousel;
