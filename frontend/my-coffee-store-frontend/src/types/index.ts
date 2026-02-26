/**
 * 用户类型
 */
export interface User {
  id: number;
  username: string;
  email: string;
  phone?: string;
  avatar?: string;
  balance?: number;
  createTime: string;
}

/**
 * 余额响应类型
 */
export interface BalanceResponse {
  balance: number;
}

/**
 * 咖啡产品类型
 */
export interface Coffee {
  coffeeId: number;
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  category: string;
  imageUrl: string;
  stock: number;
  status: number;
  rating?: number;
  brewingTime?: number; // 冲泡时间（分钟）
  size?: string[];
}

/**
 * 咖啡分类
 */
export type CoffeeCategory = 'espresso' | 'latte' | 'cappuccino' | 'americano' | 'mocha' | 'other';

/**
 * 购物车项目类型（后端返回格式）
 */
export interface CartItem {
  cartId: number;
  coffeeId: number;
  coffeeName: string;
  imageUrl: string;
  price: number;
  quantity: number;
  subtotal: number;
  stock: number;
  status: string;
}

/**
 * 购物车项目类型（本地兼容，用于 UI 展示）
 * @deprecated 请使用 CartItem，已适配后端字段
 */
export interface LocalCartItem {
  id: number;
  coffeeId: number;
  coffee: Coffee;
  quantity: number;
  size?: string;
  price: number;
}

/**
 * 订单类型
 */
export interface Order {
  orderId?: string;  // 后端列表返回
  orderNo?: string;  // 兼容旧字段
  id?: number;       // 兼容旧字段
  userId?: number;
  totalPrice?: number;       // 兼容旧字段
  totalAmount?: number;      // 后端返回
  status: OrderStatus;
  orderType: OrderType;
  items?: OrderItem[];
  createTime: string;
  updateTime?: string;
  remark?: string;
}

/**
 * 订单状态
 */
export type OrderStatus = 'pending' | 'confirmed' | 'preparing' | 'ready' | 'completed' | 'cancelled';

/**
 * 订单类型
 */
export type OrderType = 'dine_in' | 'takeout' | 'delivery';

/**
 * 订单详情项类型
 */
export interface OrderItem {
  itemId?: number;      // 后端返回
  id?: number;          // 兼容旧字段
  orderId?: number;
  coffeeId: number;
  coffeeName: string;
  coffeeImage?: string;
  imageUrl?: string;
  quantity: number;
  price: number;
  subtotal?: number;    // 后端返回
  size?: string;
}

/**
 * 购物车响应类型（后端返回）
 */
export interface CartResponse {
  totalQuantity: number;
  totalPrice: number;
  items: CartItem[];
}

/**
 * 用户登录请求类型
 */
export interface LoginRequest {
  account: string;
  password: string;
}

/**
 * 用户注册请求类型
 */
export interface RegisterRequest {
  username: string;
  email: string;
  phone?: string;
  password: string;
  confirmPassword: string;
}

/**
 * API 响应类型
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

/**
 * 分页请求参数
 */
export interface PageParams {
  page: number;
  size: number;
}

/**
 * 分页响应类型
 */
export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}

/**
 * 路由配置类型
 */
export interface RouteConfig {
  path: string;
  component: React.ComponentType;
  title: string;
  requireAuth?: boolean;
}

/**
 * 购物车状态类型
 */
export interface CartState {
  items: CartItem[];
  totalQuantity: number;
  totalPrice: number;
}

/**
 * 用户状态类型
 */
export interface UserState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
}
