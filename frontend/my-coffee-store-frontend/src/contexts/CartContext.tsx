/**
 * 购物车上下文
 */

import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { CartItem, Coffee } from '../types';
import { storage, calculateCartTotal } from '../utils/helpers';
import { STORAGE_KEYS, COFFEE_SIZES } from '../utils/constants';

interface CartContextType {
  items: CartItem[];
  totalQuantity: number;
  totalPrice: number;
  isLoading: boolean;
  addItem: (coffee: Coffee, quantity?: number, size?: string) => void;
  removeItem: (id: number) => void;
  updateQuantity: (id: number, quantity: number) => void;
  clearCart: () => void;
  isInCart: (coffeeId: number) => boolean;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

/**
 * 购物车上下文 Provider
 */
export const CartProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [items, setItems] = useState<CartItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // 初始化：从本地存储恢复购物车
  useEffect(() => {
    const initCart = () => {
      try {
        const storedCart = storage.get<CartItem[]>(STORAGE_KEYS.CART);
        if (storedCart) {
          setItems(storedCart);
        }
      } catch (error) {
        console.error('初始化购物车失败:', error);
      } finally {
        setIsLoading(false);
      }
    };

    initCart();
  }, []);

  // 保存购物车到本地存储
  useEffect(() => {
    if (!isLoading) {
      storage.set(STORAGE_KEYS.CART, items);
    }
  }, [items, isLoading]);

  // 计算总数量和总价
  const totalQuantity = items.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = calculateCartTotal(items);

  /**
   * 添加商品到购物车
   */
  const addItem = useCallback((coffee: Coffee, quantity: number = 1, size: string = 'M') => {
    setItems((prevItems) => {
      // 查找是否已存在该商品
      const existingItemIndex = prevItems.findIndex(
        (item) => item.coffeeId === coffee.id && item.size === size
      );

      if (existingItemIndex > -1) {
        // 已存在，更新数量
        const updatedItems = [...prevItems];
        updatedItems[existingItemIndex].quantity += quantity;
        return updatedItems;
      } else {
        // 不存在，添加新商品
        const sizePrice = COFFEE_SIZES.find((s) => s.value === size)?.price || 0;
        const newItem: CartItem = {
          id: Date.now(),
          coffeeId: coffee.id,
          coffee,
          quantity,
          size,
          price: coffee.price + sizePrice,
        };
        return [...prevItems, newItem];
      }
    });
  }, []);

  /**
   * 从购物车移除商品
   */
  const removeItem = useCallback((id: number) => {
    setItems((prevItems) => prevItems.filter((item) => item.id !== id));
  }, []);

  /**
   * 更新商品数量
   */
  const updateQuantity = useCallback((id: number, quantity: number) => {
    if (quantity <= 0) {
      removeItem(id);
      return;
    }

    setItems((prevItems) =>
      prevItems.map((item) =>
        item.id === id ? { ...item, quantity } : item
      )
    );
  }, [removeItem]);

  /**
   * 清空购物车
   */
  const clearCart = useCallback(() => {
    setItems([]);
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
