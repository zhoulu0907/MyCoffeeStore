import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { LlmConfigSection } from '../LlmConfigSection';
import { adminApi } from '../../../services/api';
import type { LlmConfig } from '../../../types/admin';

// 模拟 API
jest.mock('../../../services/api');

const mockAdminApi = adminApi as jest.Mocked<typeof adminApi>;

describe('LlmConfigSection', () => {
  const mockConfig: LlmConfig = {
    id: 1,
    provider: 'openai',
    baseUrl: 'https://api.openai.com/v1',
    apiKey: 'sk-1234567890',
    model: 'gpt-3.5-turbo',
    temperature: 0.7,
    maxTokens: 2048,
    enabled: true,
  };

  beforeEach(() => {
    jest.clearAllMocks();

    // 设置默认的 mock 实现
    mockAdminApi.getLlmConfigs.mockResolvedValue({
      code: 200,
      data: {
        configs: [mockConfig],
        providers: ['openai', 'modelscope']
      },
      message: 'success'
    });

    mockAdminApi.updateLlmConfig.mockResolvedValue({
      code: 200,
      data: null,
      message: '配置保存成功'
    });

    mockAdminApi.testLlmConnection.mockResolvedValue({
      code: 200,
      data: {
        success: true,
        message: '连接成功',
        latency: 120
      },
      message: 'success'
    });
  });

  test('renders LLM configuration form', () => {
    render(<LlmConfigSection />);

    expect(screen.getByText('LLM 配置')).toBeInTheDocument();
    expect(screen.getByText('URL / API-Key / Model / Temperature / Max Tokens')).toBeInTheDocument();

    // 验证默认值是否正确设置
    expect(screen.getByDisplayValue('https://api.openai.com/v1')).toBeInTheDocument();
    expect(screen.getByDisplayValue('gpt-3.5-turbo')).toBeInTheDocument();
    expect(screen.getByDisplayValue('0.7')).toBeInTheDocument();
    expect(screen.getByDisplayValue('2048')).toBeInTheDocument();
  });

  test('displays success message when configuration is saved', async () => {
    render(<LlmConfigSection />);

    // 点击保存按钮
    fireEvent.click(screen.getByText('保存配置'));

    await waitFor(() => {
      expect(screen.getByText('配置保存成功')).toBeInTheDocument();
    });

    // 验证 API 是否被调用
    expect(mockAdminApi.updateLlmConfig).toHaveBeenCalledWith(mockConfig);
  });

  test('tests connection successfully', async () => {
    render(<LlmConfigSection />);

    // 点击测试连接按钮
    fireEvent.click(screen.getByText('测试连接'));

    await waitFor(() => {
      expect(screen.getByText('连接成功 (120ms)')).toBeInTheDocument();
    });

    // 验证 API 是否被调用
    expect(mockAdminApi.testLlmConnection).toHaveBeenCalledWith('openai');
  });

  test('displays error message when connection test fails', async () => {
    // 模拟连接测试失败
    mockAdminApi.testLlmConnection.mockResolvedValue({
      code: 400,
      data: {
        success: false,
        message: 'API key is invalid'
      },
      message: '连接失败'
    });

    render(<LlmConfigSection />);

    // 点击测试连接按钮
    fireEvent.click(screen.getByText('测试连接'));

    await waitFor(() => {
      expect(screen.getByText('连接失败')).toBeInTheDocument();
    });
  });

  test('disables save button while saving', async () => {
    // 模拟保存过程需要一些时间
    mockAdminApi.updateLlmConfig.mockImplementation(() =>
      new Promise(resolve => setTimeout(resolve, 1000))
    );

    render(<LlmConfigSection />);

    // 点击保存按钮
    fireEvent.click(screen.getByText('保存配置'));

    // 验证保存按钮被禁用
    expect(screen.getByText('保存中...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '保存中...' })).toBeDisabled();

    // 等待保存完成
    await act(async () => {
      await new Promise(resolve => setTimeout(resolve, 1000));
    });

    await waitFor(() => {
      expect(screen.getByText('保存配置')).toBeInTheDocument();
    });
  });

  test('updates configuration fields correctly', () => {
    render(<LlmConfigSection />);

    // 更新 Base URL
    const baseUrlInput = screen.getByLabelText('Base URL');
    fireEvent.change(baseUrlInput, { target: { value: 'https://new-api.example.com/v1' } });
    expect(baseUrlInput).toHaveValue('https://new-api.example.com/v1');

    // 更新 API Key
    const apiKeyInput = screen.getByLabelText('API Key');
    fireEvent.change(apiKeyInput, { target: { value: 'new-api-key' } });
    expect(apiKeyInput).toHaveValue('new-api-key');

    // 更新 Model
    const modelInput = screen.getByLabelText('Model');
    fireEvent.change(modelInput, { target: { value: 'gpt-4' } });
    expect(modelInput).toHaveValue('gpt-4');

    // 更新 Temperature
    const temperatureInput = screen.getByLabelText('Temperature');
    fireEvent.change(temperatureInput, { target: { value: '0.9' } });
    expect(temperatureInput).toHaveValue('0.9');

    // 更新 Max Tokens
    const maxTokensInput = screen.getByLabelText('Max Tokens');
    fireEvent.change(maxTokensInput, { target: { value: '4096' } });
    expect(maxTokensInput).toHaveValue('4096');
  });

  test('handles validation errors', async () => {
    // 模拟 API 返回验证错误
    mockAdminApi.updateLlmConfig.mockResolvedValue({
      code: 400,
      data: null,
      message: 'API key is required'
    });

    render(<LlmConfigSection />);

    // 清空 API Key
    const apiKeyInput = screen.getByLabelText('API Key');
    fireEvent.change(apiKeyInput, { target: { value: '' } });

    // 尝试保存
    fireEvent.click(screen.getByText('保存配置'));

    await waitFor(() => {
      expect(screen.getByText('配置保存失败')).toBeInTheDocument();
    });
  });

  test('loads configuration on initial render', () => {
    render(<LlmConfigSection />);

    // 验证初始加载状态
    expect(screen.getByText('LLM 配置')).toBeInTheDocument();

    // 验证默认配置是否加载
    expect(screen.getByDisplayValue('https://api.openai.com/v1')).toBeInTheDocument();
    expect(screen.getByDisplayValue('gpt-3.5-turbo')).toBeInTheDocument();
  });

  test('handles API error when loading configurations', async () => {
    // 模拟加载配置失败
    mockAdminApi.getLlmConfigs.mockRejectedValue(new Error('Network error'));

    render(<LlmConfigSection />);

    // 验证错误处理
    await waitFor(() => {
      // 应该保持默认配置而不显示错误
      expect(screen.getByDisplayValue('https://api.openai.com/v1')).toBeInTheDocument();
    });
  });

  test('tests connection when API key is empty', async () => {
    render(<LlmConfigSection />);

    // 清空 API Key
    const apiKeyInput = screen.getByLabelText('API Key');
    fireEvent.change(apiKeyInput, { target: { value: '' } });

    // 尝试测试连接
    fireEvent.click(screen.getByText('测试连接'));

    // 验证按钮没有被禁用（因为 API key 为空）
    expect(screen.getByText('测试连接')).toBeDisabled();

    // 验证没有调用 API
    expect(mockAdminApi.testLlmConnection).not.toHaveBeenCalled();
  });

  test('handles number input validation for temperature', () => {
    render(<LlmConfigSection />);

    const temperatureInput = screen.getByLabelText('Temperature');

    // 尝试输入无效值
    fireEvent.change(temperatureInput, { target: { value: 'invalid' } });
    expect(temperatureInput).toHaveValue('');

    // 输入有效值
    fireEvent.change(temperatureInput, { target: { value: '0.5' } });
    expect(temperatureInput).toHaveValue('0.5');

    // 输入超出范围的值
    fireEvent.change(temperatureInput, { target: { value: '3.0' } });
    expect(temperatureInput).toHaveValue('3.0'); // 注意：HTML input 不会自动限制，需要在业务逻辑中处理
  });

  test('handles number input validation for max tokens', () => {
    render(<LlmConfigSection />);

    const maxTokensInput = screen.getByLabelText('Max Tokens');

    // 尝试输入无效值
    fireEvent.change(maxTokensInput, { target: { value: 'invalid' } });
    expect(maxTokensInput).toHaveValue('');

    // 输入有效值
    fireEvent.change(maxTokensInput, { target: { value: '1024' } });
    expect(maxTokensInput).toHaveValue('1024');

    // 输入小于 1 的值
    fireEvent.change(maxTokensInput, { target: { value: '0' } });
    expect(maxTokensInput).toHaveValue('0'); // 注意：HTML input 不会自动限制，需要在业务逻辑中处理
  });
});