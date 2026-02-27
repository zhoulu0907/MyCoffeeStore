/**
 * API 服务层
 */

import axios from 'axios';
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { API_BASE_URL } from '../utils/constants';
import { tokenManager } from '../utils/helpers';

// 请求配置
const config: AxiosRequestConfig = {
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
};

// 创建 axios 实例
const axiosInstance: AxiosInstance = axios.create(config);

/**
 * 请求拦截器
 */
axiosInstance.interceptors.request.use(
  (config) => {
    console.log('[API] 发送请求:', config.method?.toUpperCase(), config.url, config.data);
    // 添加 token 到请求头
    const token = tokenManager.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('[API] 请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

/**
 * 响应拦截器
 */
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log('[API] 响应成功:', response.config.method?.toUpperCase(), response.config.url, response.data);
    return response.data;
  },
  (error: AxiosError) => {
    console.error('[API] 响应失败:', error.config?.method?.toUpperCase(), error.config?.url, error.response?.status, error.response?.data);
    // 处理错误
    if (error.response) {
      const status = error.response.status;
      switch (status) {
        case 401:
          // 未授权，清除 token 并跳转到登录页
          tokenManager.removeToken();
          window.location.href = '/login';
          break;
        case 403:
          console.error('没有权限访问');
          break;
        case 404:
          console.error('请求的资源不存在');
          break;
        case 500:
          console.error('服务器错误');
          break;
        default:
          console.error('请求失败');
      }
    } else if (error.request) {
      console.error('网络错误，请检查网络连接');
    } else {
      console.error('请求配置错误');
    }
    return Promise.reject(error);
  }
);

/**
 * GET 请求
 */
export const get = <T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> => {
  return axiosInstance.request<any, T>({
    method: 'GET',
    url,
    params,
    ...config,
  });
};

/**
 * POST 请求
 */
export const post = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
  return axiosInstance.request<any, T>({
    method: 'POST',
    url,
    data,
    ...config,
  });
};

/**
 * 用户认证 API
 */
export const userApi = {
  /**
   * 用户登录
   */
  login: (data: { account: string; password: string }) => {
    return post('/v1/auth/login', data);
  },

  /**
   * 用户注册
   */
  register: (data: { username: string; email: string; phone?: string; password: string }) => {
    return post('/v1/auth/register', data);
  },

  /**
   * 获取用户信息
   */
  getUserInfo: () => {
    return get('/v1/auth/info');
  },

  /**
   * 用户退出
   */
  logout: () => {
    return post('/v1/auth/logout');
  },

  /**
   * 查询余额
   */
  getBalance: () => {
    return get('/v1/user/balance');
  },

  /**
   * 充值
   */
  recharge: (amount: number) => {
    return post('/v1/user/recharge', { amount });
  },
};

/**
 * 咖啡产品 API
 */
export const coffeeApi = {
  /**
   * 获取咖啡列表
   */
  getList: (params?: { category?: string; page?: number; size?: number }) => {
    return get('/v1/coffee/list', params);
  },

  /**
   * 获取咖啡详情
   */
  getDetail: (coffeeId: number) => {
    return get('/v1/coffee/detail', { coffeeId });
  },

  /**
   * 获取咖啡分类
   */
  getCategories: () => {
    return get('/v1/coffee/categories');
  },
};

/**
 * 购物车 API
 */
export const cartApi = {
  /**
   * 获取购物车
   */
  getCart: () => {
    console.log('[cartApi] getCart 调用');
    return get('/v1/cart/list');
  },

  /**
   * 添加到购物车
   */
  add: (data: { coffeeId: number; quantity: number }) => {
    console.log('[cartApi] add 调用, 参数:', data);
    return post('/v1/cart/add', data);
  },

  /**
   * 更新购物车项数量
   */
  update: (data: { cartId: number; quantity: number }) => {
    console.log('[cartApi] update 调用, 参数:', data);
    return post('/v1/cart/update', data);
  },

  /**
   * 删除购物车项
   */
  remove: (cartId: number) => {
    console.log('[cartApi] remove 调用, cartId:', cartId);
    return post(`/v1/cart/remove?cartId=${cartId}`);
  },

  /**
   * 清空购物车
   */
  clear: () => {
    console.log('[cartApi] clear 调用');
    return post('/v1/cart/clear');
  },
};

/**
 * 订单 API
 */
export const orderApi = {
  /**
   * 创建订单
   */
  create: (data: {
    items: Array<{ coffeeId: number; quantity: number; price: number }>;
    orderType: string;
    remark?: string;
    deliveryAddress?: {
      address: string;
      city: string;
      state: string;
      zipCode: string;
      phone: string;
    };
  }) => {
    return post('/v1/order/create', data);
  },

  /**
   * 获取订单列表
   */
  getList: (params?: { status?: string; page?: number; size?: number }) => {
    return get('/v1/order/list', params);
  },

  /**
   * 获取订单详情
   */
  getDetail: (orderId: string) => {
    return get('/v1/order/detail', { orderId });
  },

  /**
   * 取消订单
   */
  cancel: (orderId: string, reason?: string) => {
    return post(`/v1/order/cancel?orderId=${orderId}${reason ? '&reason=' + encodeURIComponent(reason) : ''}`);
  },
};

/**
 * 推荐 API
 */
export const recommendationApi = {
  /**
   * 获取咖啡推荐
   */
  recommend: (data: { roles: string[]; preference: string }) => {
    return post('/v1/recommendation', data);
  },
};

/**
 * Agent 聊天消息
 */
export interface AgentChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

/**
 * Agent SSE 事件数据
 */
export interface AgentSSEEvent {
  type: 'text' | 'tool_call' | 'tool_result' | 'done' | 'error';
  content?: string;
  toolName?: string;
  toolArgs?: Record<string, unknown>;
  result?: unknown;
  message?: string;
}

/**
 * Agent API - SSE 流式聊天
 */
export const agentApi = {
  /**
   * Agent 流式聊天
   * 使用 fetch + ReadableStream 处理 SSE（axios 不支持流式）
   *
   * @param data 请求数据（agentType + messages）
   * @param onEvent SSE 事件回调
   * @param onError 错误回调
   * @param onComplete 完成回调
   * @returns AbortController 用于取消请求
   */
  chat: (
    data: { agentType: string; messages: AgentChatMessage[] },
    onEvent: (event: AgentSSEEvent) => void,
    onError: (error: Error) => void,
    onComplete: () => void,
  ): AbortController => {
    const token = tokenManager.getToken();
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const abortController = new AbortController();

    fetch(`${API_BASE_URL}/v1/agent/chat`, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
      signal: abortController.signal,
    })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }
        const reader = response.body?.getReader();
        if (!reader) throw new Error('无法读取响应流');

        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop() || '';

          for (const line of lines) {
            if (line.startsWith('data:')) {
              const jsonStr = line.slice(5).trim();
              if (jsonStr === '') continue;
              try {
                const event: AgentSSEEvent = JSON.parse(jsonStr);
                onEvent(event);
              } catch {
                // 忽略解析失败的行
              }
            }
          }
        }

        // 处理 buffer 中残留的最后一行数据
        if (buffer.startsWith('data:')) {
          const jsonStr = buffer.slice(5).trim();
          if (jsonStr !== '') {
            try {
              const event: AgentSSEEvent = JSON.parse(jsonStr);
              onEvent(event);
            } catch {
              // 忽略解析失败的行
            }
          }
        }
        onComplete();
      })
      .catch((error: Error) => {
        if (error.name !== 'AbortError') {
          onError(error);
        }
      });

    return abortController;
  },
};

export default axiosInstance;
