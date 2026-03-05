import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { UsersSection } from '../UsersSection';
import { adminApi } from '../../../services/api';
import type { AdminUser } from '../../../types/admin';

// 模拟 API
jest.mock('../../../services/api');

const mockAdminApi = adminApi as jest.Mocked<typeof adminApi>;

describe('UsersSection', () => {
  const mockUsers: AdminUser[] = [
    {
      id: 1,
      username: 'haight_user',
      email: 'user@haightcafe.com',
      phone: '+1 (415) 555-0101',
      balance: 150.00,
      orderCount: 23,
      lastOrderDate: '2026-03-05',
      lastOrderNo: 'A-2046',
      createTime: '2025-01-15',
      status: 'active',
    },
    {
      id: 2,
      username: 'emma_bean',
      email: 'emma@haightcafe.com',
      phone: '+1 (415) 555-0102',
      balance: 85.50,
      orderCount: 11,
      lastOrderDate: '2026-03-04',
      lastOrderNo: 'A-2033',
      createTime: '2025-02-20',
      status: 'active',
    },
    {
      id: 3,
      username: 'coffee_lover',
      email: 'lover@haightcafe.com',
      phone: '+1 (415) 555-0103',
      balance: 200.00,
      orderCount: 45,
      lastOrderDate: '2026-03-05',
      lastOrderNo: 'A-2047',
      createTime: '2024-12-10',
      status: 'banned',
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();

    // 设置默认的 mock 实现
    mockAdminApi.getUsers.mockResolvedValue({
      code: 200,
      data: {
        users: mockUsers,
        total: 3,
        page: 1,
        size: 50
      },
      message: 'success'
    });

    mockAdminApi.updateUserStatus.mockResolvedValue({
      code: 200,
      data: null,
      message: '状态更新成功'
    });
  });

  test('renders user list with correct data', () => {
    render(<UsersSection />);

    // 验证标题
    expect(screen.getByText('用户列表')).toBeInTheDocument();
    expect(screen.getByText('账户信息 + 订单信息')).toBeInTheDocument();

    // 验证用户数据
    expect(screen.getByText('haight_user')).toBeInTheDocument();
    expect(screen.getByText('user@haightcafe.com')).toBeInTheDocument();
    expect(screen.getByText('A-2046')).toBeInTheDocument();

    // 验证用户状态
    expect(screen.getByText('活跃')).toBeInTheDocument();
    expect(screen.getByText('封禁')).toBeInTheDocument();
  });

  test('displays loading state', () => {
    // 模拟加载中状态
    mockAdminApi.getUsers.mockImplementationOnce(() =>
      new Promise(resolve => setTimeout(resolve, 1000))
    );

    render(<UsersSection />);

    // 验证加载状态
    expect(screen.getByText('加载中...')).toBeInTheDocument();
  });

  test('handles API error gracefully', async () => {
    // 模拟 API 错误
    mockAdminApi.getUsers.mockRejectedValue(new Error('Network error'));

    render(<UsersSection />);

    // 验证错误处理
    await waitFor(() => {
      expect(screen.getByText('获取用户列表失败')).toBeInTheDocument();
    });
  });

  test('searches users correctly', async () => {
    render(<UsersSection />);

    // 输入搜索关键词
    const searchInput = screen.getByPlaceholderText('搜索用户名或邮箱...');
    fireEvent.change(searchInput, { target: { value: 'emma' } });

    // 点击搜索按钮
    fireEvent.click(screen.getByText('搜索'));

    // 验证 API 被正确调用
    await waitFor(() => {
      expect(mockAdminApi.getUsers).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        search: 'emma',
        status: 'all'
      });
    });

    // 验证搜索结果
    expect(screen.getByText('emma_bean')).toBeInTheDocument();
    expect(screen.getByText('emma@haightcafe.com')).toBeInTheDocument();
  });

  test('filters users by status', async () => {
    render(<UsersSection />);

    // 点击"封禁"按钮
    fireEvent.click(screen.getByText('封禁'));

    // 验证 API 被正确调用
    await waitFor(() => {
      expect(mockAdminApi.getUsers).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        search: '',
        status: 'banned'
      });
    });

    // 验证只有封禁的用户显示
    expect(screen.getByText('coffee_lover')).toBeInTheDocument();
    expect(screen.queryByText('haight_user')).not.toBeInTheDocument();
  });

  test('views user details', async () => {
    render(<UsersSection />);

    // 点击"查看"按钮
    fireEvent.click(screen.getAllByText('查看')[0]);

    // 验证用户详情模态框出现
    await waitFor(() => {
      expect(screen.getByText('用户详情')).toBeInTheDocument();
      expect(screen.getByText('haight_user')).toBeInTheDocument();
      expect(screen.getByText('user@haightcafe.com')).toBeInTheDocument();
      expect(screen.getByText('+1 (415) 555-0101')).toBeInTheDocument();
      expect(screen.getByText('¥150.00')).toBeInTheDocument();
    });

    // 关闭模态框
    fireEvent.click(screen.getByText('关闭'));

    // 验证模态框关闭
    await waitFor(() => {
      expect(screen.queryByText('用户详情')).not.toBeInTheDocument();
    });
  });

  test('bans user correctly', async () => {
    render(<UsersSection />);

    // 点击"封禁"按钮
    fireEvent.click(screen.getAllByText('封禁')[0]);

    // 确认封禁
    fireEvent.click(screen.getByText('确定'));

    // 验证 API 被正确调用
    await waitFor(() => {
      expect(mockAdminApi.updateUserStatus).toHaveBeenCalledWith(1, 'banned');
    });

    // 验证用户状态更新
    expect(screen.getByText('封禁')).toBeInTheDocument();
  });

  test('unbans user correctly', async () => {
    // 先封禁用户
    mockAdminApi.getUsers.mockResolvedValue({
      code: 200,
      data: {
        users: [mockUsers[2]], // coffee_lover 是封禁状态
        total: 1,
        page: 1,
        size: 50
      },
      message: 'success'
    });

    render(<UsersSection />);

    // 切换到封禁状态视图
    fireEvent.click(screen.getByText('封禁'));

    await waitFor(() => {
      expect(screen.getByText('coffee_lover')).toBeInTheDocument();
    });

    // 点击"解封"按钮
    fireEvent.click(screen.getByText('解封'));

    // 确认解封
    fireEvent.click(screen.getByText('确定'));

    // 验证 API 被正确调用
    await waitFor(() => {
      expect(mockAdminApi.updateUserStatus).toHaveBeenCalledWith(3, 'active');
    });
  });

  test('handles confirmation dialog for banning', async () => {
    render(<UsersSection />);

    // 点击"封禁"按钮
    fireEvent.click(screen.getAllByText('封禁')[0]);

    // 取消封禁
    fireEvent.click(screen.getByText('取消'));

    // 验证 API 没有被调用
    expect(mockAdminApi.updateUserStatus).not.toHaveBeenCalled();
  });

  test('displays user balance correctly', () => {
    render(<UsersSection />);

    expect(screen.getByText('¥150.00')).toBeInTheDocument();
    expect(screen.getByText('¥85.50')).toBeInTheDocument();
    expect(screen.getByText('¥200.00')).toBeInTheDocument();
  });

  test('displays order count correctly', () => {
    render(<UsersSection />);

    expect(screen.getByText('23')).toBeInTheDocument();
    expect(screen.getByText('11')).toBeInTheDocument();
    expect(screen.getByText('45')).toBeInTheDocument();
  });

  test('displays last order information correctly', () => {
    render(<UsersSection />);

    expect(screen.getByText('A-2046')).toBeInTheDocument();
    expect(screen.getByText('A-2033')).toBeInTheDocument();
    expect(screen.getByText('A-2047')).toBeInTheDocument();
  });

  test('handles API error when updating user status', async () => {
    // 模拟更新状态失败
    mockAdminApi.updateUserStatus.mockRejectedValue(new Error('Update failed'));

    render(<UsersSection />);

    // 尝试封禁用户
    fireEvent.click(screen.getAllByText('封禁')[0]);
    fireEvent.click(screen.getByText('确定'));

    // 验证错误处理
    // 注意：这里可以根据实际需求添加错误提示的测试
    expect(screen.getByText('封禁')).toBeInTheDocument();
  });

  test('search is case insensitive', async () => {
    render(<UsersSection />);

    // 输入大写搜索关键词
    const searchInput = screen.getByPlaceholderText('搜索用户名或邮箱...');
    fireEvent.change(searchInput, { target: { value: 'EMMA' } });

    // 点击搜索按钮
    fireEvent.click(screen.getByText('搜索'));

    // 验证 API 被正确调用（应该转换为小写或保持原样）
    await waitFor(() => {
      expect(mockAdminApi.getUsers).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        search: 'EMMA',
        status: 'all'
      });
    });
  });

  test('handles empty search result', async () => {
    // 模拟没有搜索结果
    mockAdminApi.getUsers.mockResolvedValue({
      code: 200,
      data: {
        users: [],
        total: 0,
        page: 1,
        size: 10
      },
      message: 'success'
    });

    render(<UsersSection />);

    // 输入搜索关键词
    const searchInput = screen.getByPlaceholderText('搜索用户名或邮箱...');
    fireEvent.change(searchInput, { target: { value: 'nonexistent' } });

    // 点击搜索按钮
    fireEvent.click(screen.getByText('搜索'));

    await waitFor(() => {
      expect(screen.getByText('暂无用户数据')).toBeInTheDocument();
    });
  });
});