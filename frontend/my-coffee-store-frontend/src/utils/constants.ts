/**
 * 应用常量配置
 */

// API 基础路径（开发环境通过 vite proxy 转发到 http://localhost:8080/api）
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

// 咖啡分类配置
export const COFFEE_CATEGORIES = [
  { value: 'all', label: '全部' },
  { value: 'espresso', label: '意式浓缩系列' },
  { value: 'brew', label: '手冲系列' },
  { value: 'cold', label: '冷萃/冰咖啡' },
  { value: 'blend', label: '拼配系列' },
] as const;

// 订单状态配置
export const ORDER_STATUS = {
  pending: { label: '待确认', color: 'text-yellow-600', bgColor: 'bg-yellow-100' },
  confirmed: { label: '已确认', color: 'text-blue-600', bgColor: 'bg-blue-100' },
  preparing: { label: '制作中', color: 'text-orange-600', bgColor: 'bg-orange-100' },
  ready: { label: '已完成', color: 'text-green-600', bgColor: 'bg-green-100' },
  completed: { label: '已取餐', color: 'text-gray-600', bgColor: 'bg-gray-100' },
  cancelled: { label: '已取消', color: 'text-red-600', bgColor: 'bg-red-100' },
} as const;

// 订单类型配置
export const ORDER_TYPES = [
  { value: 'dine_in', label: '堂食' },
  { value: 'takeaway', label: '外带' },
  { value: 'delivery', label: '外卖' },
] as const;

// 咖啡尺寸配置
export const COFFEE_SIZES = [
  { value: 'S', label: '小杯', price: 0 },
  { value: 'M', label: '中杯', price: 2 },
  { value: 'L', label: '大杯', price: 4 },
] as const;

// 本地存储键名
export const STORAGE_KEYS = {
  TOKEN: 'coffee_store_token',
  USER: 'coffee_store_user',
  CART: 'coffee_store_cart',
} as const;

// 分页配置
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZES: [10, 20, 50],
} as const;

// 路由路径
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  COFFEE_LIST: '/coffee',
  COFFEE_DETAIL: '/coffee/:id',
  CART: '/cart',
  ORDER: '/order',
  ORDER_DETAIL: '/order/:id',
  PROFILE: '/profile',
} as const;

// 轮播图配置
export const CAROUSEL_ITEMS = [
  {
    id: 1,
    image: 'https://images.unsplash.com/photo-1702677413541-ffc41d8c08b3?w=1600',
    title: '精选咖啡',
    subtitle: '用心烘焙，每一杯都是艺术品',
  },
  {
    id: 2,
    image: 'https://images.unsplash.com/photo-1497935586351-b67a49e012bf?w=1600',
    title: '专业冲煮',
    subtitle: '精心冲煮，完美呈现每一杯',
  },
  {
    id: 3,
    image: 'https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=1600',
    title: '舒适环境',
    subtitle: '温馨舒适，享受美好时光',
  },
] as const;

// 模拟咖啡数据
export const MOCK_COFFEES = [
  {
    id: 1,
    name: '经典拿铁',
    description: '浓郁的意式浓缩咖啡与丝滑蒸奶的完美结合',
    price: 28,
    category: 'latte' as const,
    imageUrl: 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=600',
    stock: 100,
    status: 'active' as const,
    rating: 4.8,
    brewingTime: 5,
    size: ['S', 'M', 'L'],
  },
  {
    id: 2,
    name: '卡布奇诺',
    description: '浓缩咖啡、蒸奶和奶泡的经典三重奏',
    price: 26,
    category: 'cappuccino' as const,
    imageUrl: 'https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=600',
    stock: 85,
    status: 'active' as const,
    rating: 4.7,
    brewingTime: 5,
    size: ['S', 'M', 'L'],
  },
  {
    id: 3,
    name: '美式咖啡',
    description: '简单的浓缩咖啡加热水，纯粹咖啡原香',
    price: 18,
    category: 'americano' as const,
    imageUrl: 'https://images.unsplash.com/photo-1517701604599-bb29b5c7fa69?w=600',
    stock: 150,
    status: 'active' as const,
    rating: 4.5,
    brewingTime: 3,
    size: ['M', 'L'],
  },
  {
    id: 4,
    name: '浓缩咖啡',
    description: '强烈浓郁的意式浓缩，咖啡爱好者的首选',
    price: 15,
    category: 'espresso' as const,
    imageUrl: 'https://images.unsplash.com/photo-1510707577719-ae7c14805e3a?w=600',
    stock: 200,
    status: 'active' as const,
    rating: 4.9,
    brewingTime: 2,
    size: ['S'],
  },
  {
    id: 5,
    name: '焦糖摩卡',
    description: '浓缩咖啡、巧克力、焦糖和牛奶的甜蜜融合',
    price: 32,
    category: 'mocha' as const,
    imageUrl: 'https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?w=600',
    stock: 70,
    status: 'active' as const,
    rating: 4.8,
    brewingTime: 6,
    size: ['M', 'L'],
  },
  {
    id: 6,
    name: '燕麦拿铁',
    description: '植物奶与浓缩咖啡的健康选择',
    price: 30,
    category: 'latte' as const,
    imageUrl: 'https://images.unsplash.com/photo-1553909489-cd47e0907980?w=600',
    stock: 60,
    status: 'active' as const,
    rating: 4.6,
    brewingTime: 5,
    size: ['M', 'L'],
  },
] as const;
