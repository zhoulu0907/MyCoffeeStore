/**
 * 加载中组件
 */

import React from 'react';

interface LoadingProps {
  size?: 'small' | 'medium' | 'large';
  text?: string;
}

const Loading: React.FC<LoadingProps> = ({ size = 'medium', text }) => {
  const sizeClasses = {
    small: 'w-4 h-4',
    medium: 'w-8 h-8',
    large: 'w-12 h-12',
  };

  return (
    <div className="flex flex-col items-center justify-center p-8">
      <div className="relative">
        <div
          className={`${sizeClasses[size]} border-4 border-gray-200 border-t-accent rounded-full animate-spin`}
        />
      </div>
      {text && (
        <p className="mt-4 text-text-secondary text-sm">{text}</p>
      )}
    </div>
  );
};

export default Loading;
