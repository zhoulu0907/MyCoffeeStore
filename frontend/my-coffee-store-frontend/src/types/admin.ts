/**
 * 管理员页面类型定义
 */

/**
 * LLM 配置类型
 */
export interface LlmConfig {
  id?: number;
  provider: string;
  baseUrl: string;
  apiKey: string;
  model: string;
  temperature: number;
  maxTokens: number;
  enabled?: boolean;
}

/**
 * LLM 提供商类型
 */
export interface LlmProvider {
  name: string;
  displayName: string;
  defaultBaseUrl: string;
  defaultModel: string;
  defaultTemperature: number;
  defaultMaxTokens: number;
}

/**
 * 用户信息（管理员视图）
 */
export interface AdminUser {
  id: number;
  username: string;
  email: string;
  phone?: string;
  balance?: number;
  orderCount: number;
  lastOrderDate?: string;
  lastOrderNo?: string;
  createTime: string;
  status: 'active' | 'inactive' | 'banned';
}

/**
 * 用户列表响应
 */
export interface UserListResponse {
  users: AdminUser[];
  total: number;
  page: number;
  size: number;
}

/**
 * LLM 配置 API 响应
 */
export interface LlmConfigResponse {
  configs: LlmConfig[];
  providers: string[];
}

/**
 * 测试连接响应
 */
export interface TestConnectionResponse {
  success: boolean;
  message: string;
  latency?: number;
}
