/**
 * 购物车上下文
 * 与后端购物车 API 交互，保持前后端数据同步
 */

import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import type { CartItem, CartResponse, ApiResponse } from '../types';
import { cartApi } from '../services/api';
import { useAuth } from './AuthContext';

interface CartContextType {
  items: CartItem[];
  totalQuantity: number;
  totalPrice: number;
  isLoading: boolean;
  addItem: (coffeeId: number, quantity?: number) => Promise<void>;
  removeItem: (cartId: number) => Promise<void>;
  updateQuantity: (cartId: number, quantity: number) => Promise<void>;
  clearCart: () => Promise<void>;
  isInCart: (coffeeId: number) => boolean;
  refreshCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

/**
 * 购物车上下文 Provider
 */
export const CartProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [items, setItems] = useState<CartItem[]>([]);
  const [totalQuantity, setTotalQuantity] = useState(0);
  const [totalPrice, setTotalPrice] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const { isAuthenticated } = useAuth();

  /**
   * 从后端刷新购物车数据
   */
  const refreshCart = useCallback(async () => {
    if (!isAuthenticated) {
      // 未登录时清空购物车状态
      setItems([]);
      setTotalQuantity(0);
      setTotalPrice(0);
      return;
    }

    try {
      setIsLoading(true);
      const response = await cartApi.getCart() as unknown as ApiResponse<CartResponse>;

      if (response.code === 200 && response.data) {
        setItems(response.data.items || []);
        setTotalQuantity(response.data.totalQuantity || 0);
        setTotalPrice(response.data.totalPrice || 0);
      }
    } catch (error) {
      console.error('获取购物车失败:', error);
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated]);

  // 登录状态变化时刷新购物车
  useEffect(() => {
    refreshCart();
  }, [refreshCart]);

  /**
   * 添加商品到购物车
   */
  const addItem = useCallback(async (coffeeId: number, quantity: number = 1) => {
    try {
      const response = await cartApi.add({ coffeeId, quantity }) as unknown as ApiResponse;

      if (response.code === 200) {
        // 添加成功后刷新购物车
        await refreshCart();
      } else {
        console.error('添加购物车失败:', response.message);
      }
    } catch (error) {
      console.error('添加购物车失败:', error);
    }
  }, [refreshCart]);

  /**
   * 从购物车移除商品
   */
  const removeItem = useCallback(async (cartId: number) => {
    try {
      const response = await cartApi.remove(cartId) as unknown as ApiResponse;

      if (response.code === 200) {
        // 移除成功后刷新购物车
        await refreshCart();
      } else {
        console.error('移除购物车项失败:', response.message);
      }
    } catch (error) {
      console.error('移除购物车项失败:', error);
    }
  }, [refreshCart]);

  /**
   * 更新商品数量
   */
  const updateQuantity = useCallback(async (cartId: number, quantity: number) => {
    if (quantity <= 0) {
      await removeItem(cartId);
      return;
    }

    try {
      const response = await cartApi.update({ cartId, quantity }) as unknown as ApiResponse;

      if (response.code === 200) {
        // 更新成功后刷新购物车
        await refreshCart();
      } else {
        console.error('更新购物车数量失败:', response.message);
      }
    } catch (error) {
      console.error('更新购物车数量失败:', error);
    }
  }, [removeItem, refreshCart]);

  /**
   * 清空购物车
   */
  const clearCart = useCallback(async () => {
    try {
      const response = await cartApi.clear() as unknown as ApiResponse;

      if (response.code === 200) {
        setItems([]);
        setTotalQuantity(0);
        setTotalPrice(0);
      } else {
        console.error('清空购物车失败:', response.message);
      }
    } catch (error) {
      console.error('清空购物车失败:', error);
    }
  }, []);

  /**
   * 检查商品是否在购物车中
   */
  const isInCart = useCallback((coffeeId: number) => {
    return items.some((item) => item.coffeeId === coffeeId);
  }, [items]);

  const value: CartContextType = {
    items,
    totalQuantity,
    totalPrice,
    isLoading,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    isInCart,
    refreshCart,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

/**
 * 使用购物车上下文的 Hook
 */
export const useCart = (): CartContextType => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart 必须在 CartProvider 内部使用');
  }
  return context;
};
