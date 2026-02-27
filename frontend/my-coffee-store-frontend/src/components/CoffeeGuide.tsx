/**
 * 咖啡向导组件 - AI 推荐对话框
 */

import React, { useState } from 'react';
import { recommendationApi } from '../services/api';
import { useCoffeeGuide } from '../contexts/CoffeeGuideContext';
import type { ApiResponse } from '../types';

export interface CoffeeRole {
  id: string;
  label: string;
  color: string;
}

export interface GuideMessage {
  id: string;
  role: 'assistant' | 'user';
  content: string;
  timestamp: Date;
  type?: 'text' | 'loading' | 'error';
}

const COFFEE_ROLES: CoffeeRole[] = [
  { id: 'beginner', label: '咖啡新手', color: '#2A1A15' },
  { id: 'energy', label: '上班提神', color: '#5A4036' },
  { id: 'drip', label: '手冲玩家', color: '#8A6A58' },
];

const MAX_SELECTED_ROLES = 3;

const CoffeeGuide: React.FC = () => {
  const { isExpanded, toggle } = useCoffeeGuide();
  const [selectedRoles, setSelectedRoles] = useState<string[]>(['beginner']);
  const [messages, setMessages] = useState<GuideMessage[]>([
    {
      id: '1',
      role: 'assistant',
      content: '你好，我可以根据你的偏好快速推荐 3 款咖啡。选择一个角色开始吧！',
      timestamp: new Date(),
    },
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // handleToggle 已移除，直接使用 context 的 toggle

  const handleRoleToggle = (roleId: string) => {
    setSelectedRoles((prev) => {
      if (prev.includes(roleId)) {
        return prev.filter((id) => id !== roleId);
      }
      if (prev.length < MAX_SELECTED_ROLES) {
        return [...prev, roleId];
      }
      return prev;
    });
  };

  const handleSendMessage = async () => {
    if (!inputValue.trim() || isLoading) return;

    const userMessage: GuideMessage = {
      id: Date.now().toString(),
      role: 'user',
      content: inputValue,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    const currentInput = inputValue;
    setInputValue('');

    // 添加加载消息
    const loadingMessage: GuideMessage = {
      id: (Date.now() + 1).toString(),
      role: 'assistant',
      content: '正在分析你的偏好...',
      timestamp: new Date(),
      type: 'loading',
    };
    setMessages((prev) => [...prev, loadingMessage]);

    setIsLoading(true);

    try {
      const response = await recommendationApi.recommend({
        roles: selectedRoles,
        preference: currentInput,
      }) as ApiResponse<{ recommendations: string; coffeeIds?: number[] }>;

      // 移除加载消息
      setMessages((prev) => prev.filter((msg) => msg.id !== loadingMessage.id));

      // 添加 AI 响应
      const aiResponse: GuideMessage = {
        id: (Date.now() + 2).toString(),
        role: 'assistant',
        content: response.data?.recommendations || '抱歉，暂时无法生成推荐，请稍后再试。',
        timestamp: new Date(),
        type: 'text',
      };
      setMessages((prev) => [...prev, aiResponse]);
    } catch (error) {
      console.error('[CoffeeGuide] 推荐请求失败:', error);

      // 移除加载消息
      setMessages((prev) => prev.filter((msg) => msg.id !== loadingMessage.id));

      // 添加错误消息
      const errorMessage: GuideMessage = {
        id: (Date.now() + 2).toString(),
        role: 'assistant',
        content: '抱歉，网络出现问题，请检查连接后重试。',
        timestamp: new Date(),
        type: 'error',
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const getRoleColor = (roleId: string) => {
    return COFFEE_ROLES.find((r) => r.id === roleId)?.color || '#DCCCB9';
  };

  if (!isExpanded) {
    return (
      <div
        className="fixed bottom-4 right-4 w-[calc(100vw-32px)] max-w-[340px] rounded-2xl shadow-2xl bg-surface-light cursor-pointer transition-all"
        style={{
          boxShadow: '0 8px 24px rgba(0,0,0,0.13)',
        }}
        onClick={toggle}
      >
        {/* 收起状态 */}
        <div className="p-3">
          {/* 拖动指示条 */}
          <div className="flex justify-center mb-2">
            <div className="w-16 h-1 bg-gold rounded-full" />
          </div>

          {/* 标题栏 */}
          <div className="flex items-center justify-between mb-3">
            <span className="text-lg font-bold text-primary">咖啡向导</span>
            <svg
              className="w-5 h-5 text-text-light"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M5 15l7-7 7 7"
              />
            </svg>
          </div>

          {/* 已选角色 */}
          <div className="flex gap-1.5 mb-3">
            {selectedRoles.map((roleId) => (
              <div
                key={roleId}
                className="h-6 rounded-full flex-1 flex items-center justify-center"
                style={{ backgroundColor: getRoleColor(roleId) }}
              >
                <div className="w-3.5 h-3.5 rounded-full bg-white/30" />
              </div>
            ))}
            <button
              className="h-6 rounded-full flex-1 flex items-center justify-center bg-accent-light"
              onClick={(e) => {
                e.stopPropagation();
                toggle();
              }}
            >
              <span className="text-lg text-text-light">+</span>
            </button>
          </div>

          {/* 提示文字 */}
          <p className="text-xs text-text-light leading-relaxed mb-3">
            告诉我你喜欢的风味（如：不太酸、偏坚果、适合下午），我来推荐。
          </p>

          {/* 消息区域 */}
          <div className="bg-white rounded-xl p-2.5 space-y-2">
            {messages.map((msg) => (
              <p
                key={msg.id}
                className={`text-xs leading-relaxed ${
                  msg.type === 'loading'
                    ? 'text-text-muted italic'
                    : msg.type === 'error'
                    ? 'text-red-600'
                    : 'text-text-secondary'
                }`}
              >
                {msg.role === 'assistant' ? '咖咖：' : ''}{msg.content}
              </p>
            ))}
          </div>

          {/* 输入区域 - 优化触摸目标尺寸为 44px */}
          <div className="flex gap-2 mt-2">
            <input
              type="text"
              placeholder="输入你的口味偏好..."
              className="flex-1 h-11 bg-white rounded-xl px-2.5 text-xs text-text-muted placeholder-text-muted focus:outline-none disabled:opacity-50"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && !isLoading && handleSendMessage()}
              onClick={(e) => e.stopPropagation()}
              disabled={isLoading}
            />
            <button
              className={`w-[74px] h-11 rounded-xl text-background text-xs font-bold transition-opacity ${
                isLoading || !inputValue.trim() ? 'opacity-50 cursor-not-allowed' : 'hover:opacity-90'
              } bg-primary`}
              onClick={(e) => {
                e.stopPropagation();
                handleSendMessage();
              }}
              disabled={isLoading || !inputValue.trim()}
            >
              {isLoading ? '发送中...' : '发送'}
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div
      className="fixed bottom-4 right-4 w-[calc(100vw-32px)] max-w-[340px] rounded-2xl shadow-2xl bg-surface-light"
      style={{
        boxShadow: '0 8px 24px rgba(0,0,0,0.13)',
      }}
    >
      {/* 展开状态 */}
      <div className="p-3">
        {/* 拖动指示条 */}
        <div className="flex justify-center mb-2">
          <div className="w-16 h-1 bg-gold rounded-full cursor-pointer" onClick={toggle} />
        </div>

        {/* 标题栏 */}
        <div className="flex items-center justify-between mb-3 h-8">
          <span className="text-lg font-bold text-primary">咖啡向导</span>
          <div className="flex items-center gap-1.5">
            {COFFEE_ROLES.map((role) => (
              <div
                key={role.id}
                className={`h-6 rounded-full flex-1 flex items-center justify-center cursor-pointer transition-all ${
                  selectedRoles.includes(role.id)
                    ? 'opacity-100'
                    : 'opacity-50 hover:opacity-70'
                }`}
                style={{ backgroundColor: role.color }}
                onClick={() => handleRoleToggle(role.id)}
              >
                <div className="w-3.5 h-3.5 rounded-full bg-white/30" />
              </div>
            ))}
            <button
              className="h-6 rounded-full w-6 flex items-center justify-center bg-accent-light"
              onClick={toggle}
            >
              <span className="text-sm text-text-light">−</span>
            </button>
          </div>
        </div>

        {/* 角色选择区域 - 优化触摸目标尺寸为 44px */}
        <div className="bg-white rounded-xl p-2.5 space-y-2 mb-3">
          <p className="text-xs font-bold text-text-secondary">选择角色</p>
          {COFFEE_ROLES.map((role) => (
            <button
              key={role.id}
              className={`w-full h-11 rounded-lg flex items-center gap-2 px-3 transition-all ${
                selectedRoles.includes(role.id)
                  ? 'bg-surface-light'
                  : 'hover:bg-surface-light/50'
              }`}
              style={{
                backgroundColor: selectedRoles.includes(role.id) ? '#F7F1E8' : 'transparent',
              }}
              onClick={() => handleRoleToggle(role.id)}
            >
              <div
                className="w-3.5 h-3.5 rounded-full"
                style={{ backgroundColor: role.color }}
              />
              <span
                className={`text-xs ${
                  selectedRoles.includes(role.id) ? 'font-semibold text-text-secondary' : 'text-text-secondary'
                }`}
              >
                {role.label}
              </span>
            </button>
          ))}
          <p className="text-[11px] text-text-muted">最多可选择 3 个角色</p>
        </div>

        {/* 提示文字 */}
        <p className="text-xs text-text-light leading-relaxed mb-3">
          告诉我你喜欢的风味（如：不太酸、偏坚果、适合下午），我来推荐。
        </p>

        {/* 消息区域 */}
        <div className="bg-white rounded-xl p-2.5 space-y-2 mb-2 max-h-[178px] overflow-y-auto">
          {messages.map((msg) => (
            <p
              key={msg.id}
              className={`text-xs leading-relaxed ${
                msg.type === 'loading'
                  ? 'text-text-muted italic'
                  : msg.type === 'error'
                  ? 'text-red-600'
                  : 'text-text-secondary'
              }`}
            >
              {msg.role === 'assistant' && <span className="font-semibold">咖咖：</span>}
              {msg.content}
            </p>
          ))}
        </div>

        {/* 输入区域 - 优化触摸目标尺寸为 44px */}
        <div className="flex gap-2">
          <input
            type="text"
            placeholder="输入你的口味偏好..."
            className="flex-1 h-11 bg-white rounded-xl px-2.5 text-xs text-text-muted placeholder-text-muted focus:outline-none disabled:opacity-50"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && !isLoading && handleSendMessage()}
            disabled={isLoading}
          />
          <button
            className={`w-[74px] h-11 rounded-xl text-background text-xs font-bold transition-opacity ${
              isLoading || !inputValue.trim() ? 'opacity-50 cursor-not-allowed' : 'hover:opacity-90'
            } bg-primary`}
            onClick={handleSendMessage}
            disabled={isLoading || !inputValue.trim()}
          >
            {isLoading ? '发送中...' : '发送'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CoffeeGuide;
