/**
 * 咖啡向导组件 - Agent 流式对话
 */

import React, { useState } from 'react';
import { agentApi } from '../services/api';
import type { AgentChatMessage } from '../services/api';
import { useCoffeeGuide } from '../contexts/CoffeeGuideContext';

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
  type?: 'text' | 'loading' | 'error' | 'tool_status';
}

const COFFEE_ROLES: CoffeeRole[] = [
  { id: 'coffee_advisor', label: '咖啡顾问', color: '#2A1A15' },
  { id: 'customer_service', label: '客服助手', color: '#5A4036' },
  { id: 'order_assistant', label: '订单助手', color: '#8A6A58' },
];

/**
 * 根据角色 ID 返回对应的提示文字
 */
const getRoleHint = (roleId: string): string => {
  switch (roleId) {
    case 'coffee_advisor':
      return '告诉我你喜欢的风味（如：不太酸、偏坚果、适合下午），我来推荐。';
    case 'customer_service':
      return '有什么可以帮你的？门店信息、配送、退款等问题都可以问。';
    case 'order_assistant':
      return '我可以帮你查菜单、下单、查订单，试试看吧！';
    default:
      return '有什么可以帮你的？';
  }
};

const CoffeeGuide: React.FC = () => {
  const { isExpanded, toggle } = useCoffeeGuide();
  const [selectedRole, setSelectedRole] = useState<string>('coffee_advisor');
  const [messages, setMessages] = useState<GuideMessage[]>([
    {
      id: '1',
      role: 'assistant',
      content: '你好，我是你的咖啡顾问，有什么可以帮你的？',
      timestamp: new Date(),
    },
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleRoleSelect = (roleId: string) => {
    setSelectedRole(roleId);
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
    setIsLoading(true);

    // 构建对话历史（给模型的 messages）
    const chatMessages: AgentChatMessage[] = messages
      .filter((msg) => msg.type !== 'loading' && msg.type !== 'error' && msg.type !== 'tool_status')
      .map((msg) => ({ role: msg.role, content: msg.content }));
    chatMessages.push({ role: 'user', content: currentInput });

    // 创建 assistant 消息占位
    const assistantMsgId = (Date.now() + 1).toString();
    const assistantMessage: GuideMessage = {
      id: assistantMsgId,
      role: 'assistant',
      content: '',
      timestamp: new Date(),
      type: 'text',
    };
    setMessages((prev) => [...prev, assistantMessage]);

    agentApi.chat(
      { agentType: selectedRole, messages: chatMessages },
      // onEvent
      (event) => {
        switch (event.type) {
          case 'text':
            // 追加文本到 assistant 消息（打字机效果）
            setMessages((prev) =>
              prev.map((msg) =>
                msg.id === assistantMsgId
                  ? { ...msg, content: msg.content + (event.content || '') }
                  : msg
              )
            );
            break;
          case 'tool_call':
            // 显示工具调用状态
            setMessages((prev) => [
              ...prev,
              {
                id: (Date.now() + Math.random()).toString(),
                role: 'assistant' as const,
                content: `正在查询 ${event.toolName}...`,
                timestamp: new Date(),
                type: 'tool_status' as const,
              },
            ]);
            break;
          case 'tool_result':
            // 工具结果可以选择不显示，或显示简要摘要
            break;
          case 'error':
            setMessages((prev) =>
              prev.map((msg) =>
                msg.id === assistantMsgId
                  ? { ...msg, content: event.message || '抱歉，服务暂时不可用', type: 'error' as const }
                  : msg
              )
            );
            break;
          case 'done':
            break;
        }
      },
      // onError
      (error) => {
        console.error('[CoffeeGuide] Agent 请求失败:', error);
        setMessages((prev) =>
          prev.map((msg) =>
            msg.id === assistantMsgId
              ? { ...msg, content: '抱歉，网络出现问题，请检查连接后重试。', type: 'error' as const }
              : msg
          )
        );
        setIsLoading(false);
      },
      // onComplete
      () => {
        // 移除 tool_status 类型的消息
        setMessages((prev) => prev.filter((msg) => msg.type !== 'tool_status'));
        setIsLoading(false);
      },
    );
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

          {/* 当前角色色条 */}
          <div className="flex gap-1.5 mb-3">
            <div
              className="h-6 rounded-full flex-1 flex items-center justify-center"
              style={{ backgroundColor: getRoleColor(selectedRole) }}
            >
              <span className="text-[10px] text-white/80 font-medium">
                {COFFEE_ROLES.find((r) => r.id === selectedRole)?.label}
              </span>
            </div>
            <button
              className="h-6 rounded-full w-8 flex items-center justify-center bg-accent-light"
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
            {getRoleHint(selectedRole)}
          </p>

          {/* 消息区域 */}
          <div className="bg-white rounded-xl p-2.5 space-y-2">
            {messages.filter((msg) => msg.type !== 'tool_status').map((msg) => (
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

          {/* 输入区域 */}
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
                  selectedRole === role.id
                    ? 'opacity-100'
                    : 'opacity-50 hover:opacity-70'
                }`}
                style={{ backgroundColor: role.color }}
                onClick={() => handleRoleSelect(role.id)}
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

        {/* 角色选择区域 */}
        <div className="bg-white rounded-xl p-2.5 space-y-2 mb-3">
          <p className="text-xs font-bold text-text-secondary">选择角色</p>
          {COFFEE_ROLES.map((role) => (
            <button
              key={role.id}
              className={`w-full h-11 rounded-lg flex items-center gap-2 px-3 transition-all ${
                selectedRole === role.id
                  ? 'bg-surface-light'
                  : 'hover:bg-surface-light/50'
              }`}
              style={{
                backgroundColor: selectedRole === role.id ? '#F7F1E8' : 'transparent',
              }}
              onClick={() => handleRoleSelect(role.id)}
            >
              <div
                className="w-3.5 h-3.5 rounded-full"
                style={{ backgroundColor: role.color }}
              />
              <span
                className={`text-xs ${
                  selectedRole === role.id ? 'font-semibold text-text-secondary' : 'text-text-secondary'
                }`}
              >
                {role.label}
              </span>
            </button>
          ))}
        </div>

        {/* 提示文字 */}
        <p className="text-xs text-text-light leading-relaxed mb-3">
          {getRoleHint(selectedRole)}
        </p>

        {/* 消息区域 */}
        <div className="bg-white rounded-xl p-2.5 space-y-2 mb-2 max-h-[178px] overflow-y-auto">
          {messages.filter((msg) => msg.type !== 'tool_status').map((msg) => (
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

        {/* 输入区域 */}
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
