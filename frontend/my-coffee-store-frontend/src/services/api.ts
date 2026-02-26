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
    // 添加 token 到请求头
    const token = tokenManager.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * 响应拦截器
 */
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data;
  },
  (error: AxiosError) => {
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
    return get('/v1/cart/list');
  },

  /**
   * 添加到购物车
   */
  add: (data: { coffeeId: number; quantity: number }) => {
    return post('/v1/cart/add', data);
  },

  /**
   * 更新购物车项数量
   */
  update: (data: { cartId: number; quantity: number }) => {
    return post('/v1/cart/update', data);
  },

  /**
   * 删除购物车项
   */
  remove: (cartId: number) => {
    return post(`/v1/cart/remove?cartId=${cartId}`);
  },

  /**
   * 清空购物车
   */
  clear: () => {
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

export default axiosInstance;
