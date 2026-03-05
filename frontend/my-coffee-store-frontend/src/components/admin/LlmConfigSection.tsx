/**
 * LLM 配置区域组件 - 管理员页面
 */

import React, { useState, useEffect } from 'react';
import type { LlmConfig } from '../../types/admin';
import { adminApi } from '../../services/api';
import type { ApiResponse } from '../../types';
import ConfigField from './ConfigField';

const LlmConfigSection: React.FC = () => {
  const [config, setConfig] = useState<LlmConfig>({
    provider: 'openai',
    baseUrl: 'https://api.openai.com/v1',
    apiKey: '',
    model: 'gpt-3.5-turbo',
    temperature: 0.7,
    maxTokens: 2048,
    enabled: true,
  });
  const [isSaving, setIsSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState<string | null>(null);
  const [isTesting, setIsTesting] = useState(false);
  const [testResult, setTestResult] = useState<string | null>(null);

  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const response = await adminApi.getLlmConfigs() as unknown as ApiResponse<{ configs: LlmConfig[] }>;
        if (response.code === 200 && response.data?.configs?.length > 0) {
          // 使用第一个配置
          const existingConfig = response.data.configs[0];
          setConfig(existingConfig);
        }
      } catch (err) {
        console.error('获取 LLM 配置失败:', err);
        // 保持默认配置
      }
    };

    fetchConfig();
  }, []);

  const handleSave = async () => {
    try {
      setIsSaving(true);
      setSaveMessage(null);
      await adminApi.updateLlmConfig(config);
      setSaveMessage('配置保存成功');
      setTimeout(() => setSaveMessage(null), 3000);
    } catch (err) {
      console.error('保存配置失败:', err);
      setSaveMessage('配置保存失败');
      setTimeout(() => setSaveMessage(null), 3000);
    } finally {
      setIsSaving(false);
    }
  };

  const handleTestConnection = async () => {
    try {
      setIsTesting(true);
      setTestResult(null);
      const response = await adminApi.testLlmConnection(config.provider) as unknown as ApiResponse<{ success: boolean; message: string; latency?: number }>;
      if (response.data?.success) {
        setTestResult(`连接成功 (${response.data.latency ? response.data.latency + 'ms' : ''})`);
      } else {
        setTestResult('连接失败');
      }
    } catch (err) {
      console.error('测试连接失败:', err);
      setTestResult('连接失败');
    } finally {
      setIsTesting(false);
      setTimeout(() => setTestResult(null), 3000);
    }
  };

  const handleChange = (field: keyof LlmConfig) => (value: string) => {
    setConfig((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleNumberChange = (field: keyof LlmConfig) => (value: string) => {
    const numValue = parseFloat(value);
    if (!isNaN(numValue)) {
      setConfig((prev) => ({
        ...prev,
        [field]: numValue,
      }));
    }
  };

  return (
    <div
      className="rounded-xl p-[14px] flex flex-col gap-3"
      style={{ backgroundColor: '#FFFFFF' }}
    >
      {/* 标题 */}
      <div>
        <h2
          className="text-[20px] font-bold mb-1"
          style={{ color: '#2A1A15', fontFamily: 'Inter, sans-serif' }}
        >
          LLM 配置
        </h2>
        <p
          className="text-[13px] font-normal"
          style={{ color: '#7A5A4E', fontFamily: 'Inter, sans-serif' }}
        >
          URL / API-Key / Model / Temperature / Max Tokens
        </p>
      </div>

      {/* 配置字段 */}
      <div className="flex flex-col gap-3">
        {/* 第一行：Base URL 和 API Key */}
        <div className="flex gap-3">
          <ConfigField
            label="Base URL"
            value={config.baseUrl}
            onChange={handleChange('baseUrl')}
            placeholder="https://api.openai.com/v1"
            required
          />
          <ConfigField
            label="API Key"
            value={config.apiKey}
            onChange={handleChange('apiKey')}
            type="password"
            placeholder="sk-********************************"
            required
          />
        </div>

        {/* 第二行：Model 和 Temperature */}
        <div className="flex gap-3">
          <div className="flex-1 flex flex-col gap-2">
            <ConfigField
              label="Model"
              value={config.model}
              onChange={handleChange('model')}
              placeholder="gpt-3.5-turbo"
              required
            />
          </div>
          <div style={{ width: '220px' }} className="flex flex-col gap-2">
            <ConfigField
              label="Temperature"
              value={config.temperature.toString()}
              onChange={(val) => handleNumberChange('temperature')(val)}
              type="number"
              placeholder="0.7"
              step="0.1"
              min="0"
              max="2"
              required
            />
          </div>
        </div>

        {/* 第三行：Max Tokens 和按钮 */}
        <div className="flex gap-3 items-end">
          <div style={{ width: '220px' }} className="flex flex-col gap-2">
            <ConfigField
              label="Max Tokens"
              value={config.maxTokens.toString()}
              onChange={(val) => handleNumberChange('maxTokens')(val)}
              type="number"
              placeholder="2048"
              min="1"
              required
            />
          </div>

          {/* 按钮组 */}
          <div className="flex-1 flex justify-end gap-3">
            <button
              onClick={handleTestConnection}
              disabled={isTesting || !config.apiKey}
              className="h-[42px] px-4 rounded-[10px] text-[14px] font-bold transition-opacity hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
              style={{
                backgroundColor: '#5E4338',
                color: '#F7F1E8',
                fontFamily: 'Inter, sans-serif',
              }}
            >
              {isTesting ? '测试中...' : '测试连接'}
            </button>
            <button
              onClick={handleSave}
              disabled={isSaving}
              className="h-[42px] px-4 rounded-[10px] text-[14px] font-bold transition-opacity hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
              style={{
                backgroundColor: '#2A1A15',
                color: '#F7F1E8',
                fontFamily: 'Inter, sans-serif',
                minWidth: '140px',
              }}
            >
              {isSaving ? '保存中...' : '保存配置'}
            </button>
          </div>
        </div>

        {/* 消息提示 */}
        {saveMessage && (
          <div
            className="text-[13px] font-normal px-3 py-2 rounded-lg"
            style={{
              color: saveMessage.includes('成功') ? '#059669' : '#DC2626',
              backgroundColor: saveMessage.includes('成功') ? '#ECFDF5' : '#FEF2F2',
            }}
          >
            {saveMessage}
          </div>
        )}

        {testResult && (
          <div
            className="text-[13px] font-normal px-3 py-2 rounded-lg"
            style={{
              color: testResult.includes('成功') ? '#059669' : '#DC2626',
              backgroundColor: testResult.includes('成功') ? '#ECFDF5' : '#FEF2F2',
            }}
          >
            {testResult}
          </div>
        )}
      </div>
    </div>
  );
};

export default LlmConfigSection;
