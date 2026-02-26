/**
 * API 服务层
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
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
 * 通用请求方法
 */
const request = async <T = any>(
  method: 'GET' | 'POST',
  url: string,
  data?: any,
  config?: AxiosRequestConfig
): Promise<T> => {
  try {
    const response = await axiosInstance.request<T>({
      method,
      url,
      data,
      ...config,
    });
    return response;
  } catch (error) {
    throw error;
  }
};

/**
 * GET 请求
 */
export const get = <T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>('GET', url, params, config);
};

/**
 * POST 请求
 */
export const post = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
  return request<T>('POST', url, data, config);
};

/**
 * 用户相关 API
 */
export const userApi = {
  /**
   * 用户登录
   */
  login: (data: { username: string; password: string }) => {
    return post('/user/login', data);
  },

  /**
   * 用户注册
   */
  register: (data: { username: string; email: string; phone?: string; password: string }) => {
    return post('/user/register', data);
  },

  /**
   * 获取用户信息
   */
  getUserInfo: () => {
    return get('/user/info');
  },

  /**
   * 更新用户信息
   */
  updateUserInfo: (data: any) => {
    return post('/user/update', data);
  },

  /**
   * 修改密码
   */
  changePassword: (data: { oldPassword: string; newPassword: string }) => {
    return post('/user/change-password', data);
  },
};

/**
 * 咖啡产品相关 API
 */
export const coffeeApi = {
  /**
   * 获取咖啡列表
   */
  getList: (params?: { category?: string; page?: number; pageSize?: number }) => {
    return get('/coffee/list', params);
  },

  /**
   * 获取咖啡详情
   */
  getDetail: (id: number) => {
    return get(`/coffee/detail/${id}`);
  },

  /**
   * 获取精选咖啡
   */
  getFeatured: () => {
    return get('/coffee/featured');
  },

  /**
   * 搜索咖啡
   */
  search: (keyword: string) => {
    return get('/coffee/search', { keyword });
  },
};

/**
 * 购物车相关 API
 */
export const cartApi = {
  /**
   * 获取购物车
   */
  getCart: () => {
    return get('/cart/list');
  },

  /**
   * 添加到购物车
   */
  add: (data: { coffeeId: number; quantity: number; size?: string }) => {
    return post('/cart/add', data);
  },

  /**
   * 更新购物车项
   */
  update: (data: { id: number; quantity: number }) => {
    return post('/cart/update', data);
  },

  /**
   * 删除购物车项
   */
  remove: (id: number) => {
    return post('/cart/remove', { id });
  },

  /**
   * 清空购物车
   */
  clear: () => {
    return post('/cart/clear');
  },
};

/**
 * 订单相关 API
 */
export const orderApi = {
  /**
   * 创建订单
   */
  create: (data: {
    items: Array<{ coffeeId: number; quantity: number; size?: string }>;
    orderType: string;
    remark?: string;
  }) => {
    return post('/order/create', data);
  },

  /**
   * 获取订单列表
   */
  getList: (params?: { status?: string; page?: number; pageSize?: number }) => {
    return get('/order/list', params);
  },

  /**
   * 获取订单详情
   */
  getDetail: (id: number) => {
    return get(`/order/detail/${id}`);
  },

  /**
   * 取消订单
   */
  cancel: (id: number) => {
    return post('/order/cancel', { id });
  },

  /**
   * 确认收货
   */
  confirm: (id: number) => {
    return post('/order/confirm', { id });
  },
};

/**
 * 文件上传 API
 */
export const uploadApi = {
  /**
   * 上传图片
   */
  uploadImage: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return post('/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};

export default axiosInstance;
