/**
 * 认证上下文
 */

import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { User } from '../types';
import { tokenManager, storage } from '../utils/helpers';
import { STORAGE_KEYS } from '../utils/constants';
import { userApi } from '../services/api';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (user: User, token: string) => void;
  logout: () => void;
  updateUser: (user: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * 认证上下文 Provider
 */
export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // 初始化：从本地存储恢复用户信息
  useEffect(() => {
    const initAuth = () => {
      try {
        const storedUser = storage.get<User>(STORAGE_KEYS.USER);
        const token = tokenManager.getToken();

        if (storedUser && token) {
          setUser(storedUser);
        }
      } catch (error) {
        console.error('初始化认证状态失败:', error);
      } finally {
        setIsLoading(false);
      }
    };

    initAuth();
  }, []);

  /**
   * 登录
   */
  const login = (user: User, token: string) => {
    setUser(user);
    tokenManager.setToken(token);
    storage.set(STORAGE_KEYS.USER, user);
  };

  /**
   * 登出
   */
  const logout = async () => {
    try {
      // 调用后端登出 API
      await userApi.logout();
    } catch (error) {
      console.error('登出 API 调用失败:', error);
    } finally {
      // 无论 API 是否成功，都清除本地状态
      setUser(null);
      tokenManager.removeToken();
      storage.remove(STORAGE_KEYS.USER);
      storage.remove(STORAGE_KEYS.CART);
    }
  };

  /**
   * 更新用户信息
   */
  const updateUser = (userData: Partial<User>) => {
    if (user) {
      const updatedUser = { ...user, ...userData };
      setUser(updatedUser);
      storage.set(STORAGE_KEYS.USER, updatedUser);
    }
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * 使用认证上下文的 Hook
 */
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth 必须在 AuthProvider 内部使用');
  }
  return context;
};
