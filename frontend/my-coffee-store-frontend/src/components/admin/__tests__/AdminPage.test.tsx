import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { AdminPage } from '../AdminPage';
import { LlmConfigSection } from '../LlmConfigSection';
import { UsersSection } from '../UsersSection';

// 模拟子组件
jest.mock('../LlmConfigSection');
jest.mock('../UsersSection');

const MockLlmConfigSection = LlmConfigSection as jest.MockedFunction<typeof LlmConfigSection>;
const MockUsersSection = UsersSection as jest.MockedFunction<typeof UsersSection>;

describe('AdminPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();

    // 设置默认的 mock 实现
    MockLlmConfigSection.mockReturnValue(<div>LLM Config Section</div>);
    MockUsersSection.mockReturnValue(<div>Users Section</div>);
  });

  test('renders admin page header', () => {
    render(<AdminPage />);

    expect(screen.getByText('管理员控制台')).toBeInTheDocument();
    expect(screen.getByText('管理 AI 模型配置和用户信息')).toBeInTheDocument();
  });

  test('initially shows LLM config section', () => {
    render(<AdminPage />);

    expect(screen.getByText('LLM Config Section')).toBeInTheDocument();
    expect(screen.queryByText('Users Section')).not.toBeInTheDocument();
  });

  test('switches to users section when users tab is clicked', () => {
    render(<AdminPage />);

    // 点击用户管理标签
    fireEvent.click(screen.getByText('用户管理'));

    expect(screen.getByText('Users Section')).toBeInTheDocument();
    expect(screen.queryByText('LLM Config Section')).not.toBeInTheDocument();
  });

  test('switches back to config section when config tab is clicked', () => {
    render(<AdminPage />);

    // 先切换到用户管理
    fireEvent.click(screen.getByText('用户管理'));
    expect(screen.getByText('Users Section')).toBeInTheDocument();

    // 再切换回 AI 配置
    fireEvent.click(screen.getByText('AI 配置'));
    expect(screen.getByText('LLM Config Section')).toBeInTheDocument();
    expect(screen.queryByText('Users Section')).not.toBeInTheDocument();
  });

  test('highlights active tab correctly', () => {
    render(<AdminPage />);

    // 验证 AI 配置标签是激活状态
    const configTab = screen.getByText('AI 配置');
    expect(configTab).toHaveClass('border-blue-500');
    expect(configTab).toHaveClass('text-blue-600');

    // 点击用户管理标签
    fireEvent.click(screen.getByText('用户管理'));

    // 验证用户管理标签是激活状态
    const usersTab = screen.getByText('用户管理');
    expect(usersTab).toHaveClass('border-blue-500');
    expect(usersTab).toHaveClass('text-blue-600');

    // 验证 AI 配置标签不再是激活状态
    expect(configTab).not.toHaveClass('border-blue-500');
    expect(configTab).not.toHaveClass('text-blue-600');
  });

  test('shows loading state initially', () => {
    // 模拟子组件的加载状态
    MockLlmConfigSection.mockReturnValue(<div>Loading...</div>);

    render(<AdminPage />);

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  test('renders LlmConfigSection component', () => {
    render(<AdminPage />);

    expect(MockLlmConfigSection).toHaveBeenCalled();
  });

  test('renders UsersSection component when users tab is active', () => {
    render(<AdminPage />);

    // 点击用户管理标签
    fireEvent.click(screen.getByText('用户管理'));

    expect(MockUsersSection).toHaveBeenCalled();
  });

  test('does not render both sections simultaneously', () => {
    render(<AdminPage />);

    // 初始状态只显示 LLM 配置
    expect(screen.getByText('LLM Config Section')).toBeInTheDocument();
    expect(screen.queryByText('Users Section')).not.toBeInTheDocument();

    // 切换到用户管理
    fireEvent.click(screen.getByText('用户管理'));
    expect(screen.getByText('Users Section')).toBeInTheDocument();
    expect(screen.queryByText('LLM Config Section')).not.toBeInTheDocument();
  });

  test('maintains tab state across re-renders', () => {
    const { rerender } = render(<AdminPage />);

    // 初始状态是 AI 配置
    expect(screen.getByText('LLM Config Section')).toBeInTheDocument();

    // 切换到用户管理
    fireEvent.click(screen.getByText('用户管理'));
    expect(screen.getByText('Users Section')).toBeInTheDocument();

    // 重新渲染，保持用户管理标签
    rerender(<AdminPage />);
    expect(screen.getByText('Users Section')).toBeInTheDocument();
    expect(screen.queryByText('LLM Config Section')).not.toBeInTheDocument();
  });

  test('handles tab navigation with keyboard', () => {
    render(<AdminPage />);

    // 使用键盘导航（如果需要的话）
    // 这里可以根据实际实现添加键盘事件的测试
  });

  test('shows correct styling for active and inactive tabs', () => {
    render(<AdminPage />);

    const configTab = screen.getByText('AI 配置');
    const usersTab = screen.getByText('用户管理');

    // 验证激活状态的样式
    expect(configTab).toHaveClass('border-blue-500');
    expect(configTab).toHaveClass('text-blue-600');

    // 验证非激活状态的样式
    expect(usersTab).toHaveClass('border-transparent');
    expect(usersTab).toHaveClass('text-gray-500');

    // 切换标签
    fireEvent.click(usersTab);

    // 验证样式更新
    expect(usersTab).toHaveClass('border-blue-500');
    expect(usersTab).toHaveClass('text-blue-600');
    expect(configTab).toHaveClass('border-transparent');
    expect(configTab).toHaveClass('text-gray-500');
  });

  test('clicks on tab should work when mouse moves fast', () => {
    render(<AdminPage />);

    // 快速点击标签
    fireEvent.click(screen.getByText('用户管理'));
    expect(screen.getByText('Users Section')).toBeInTheDocument();

    // 再次点击
    fireEvent.click(screen.getByText('AI 配置'));
    expect(screen.getByText('LLM Config Section')).toBeInTheDocument();
  });

  test('tab navigation should work when child components have interactive elements', () => {
    render(<AdminPage />);

    // 点击 AI 配置标签
    fireEvent.click(screen.getByText('AI 配置'));
    expect(screen.getByText('LLM Config Section')).toBeInTheDocument();

    // 点击用户管理标签
    fireEvent.click(screen.getByText('用户管理'));
    expect(screen.getByText('Users Section')).toBeInTheDocument();
  });

  test('should not crash when tab state changes rapidly', () => {
    render(<AdminPage />);

    // 快速切换标签
    fireEvent.click(screen.getByText('用户管理'));
    fireEvent.click(screen.getByText('AI 配置'));
    fireEvent.click(screen.getByText('用户管理'));
    fireEvent.click(screen.getByText('AI 配置'));

    // 应该保持正确的激活状态
    expect(screen.getByText('LLM Config Section')).toBeInTheDocument();
  });

  test('accessible tab navigation', () => {
    render(<AdminPage />);

    // 验证标签具有正确的 role
    const configTab = screen.getByText('AI 配置');
    const usersTab = screen.getByText('用户管理');

    // 注意：这里需要根据实际的实现添加 role 属性的测试
    // 例如：expect(configTab).toHaveAttribute('role', 'tab');
  });
});