/**
 * 用户类型
 */
export interface User {
  id: number;
  username: string;
  email: string;
  phone?: string;
  avatar?: string;
  createTime: string;
}

/**
 * 咖啡产品类型
 */
export interface Coffee {
  id: number;
  name: string;
  description: string;
  price: number;
  category: CoffeeCategory;
  imageUrl: string;
  stock: number;
  status: 'active' | 'inactive';
  rating?: number;
  brewingTime?: number; // 冲泡时间（分钟）
  size?: string[];
}

/**
 * 咖啡分类
 */
export type CoffeeCategory = 'espresso' | 'latte' | 'cappuccino' | 'americano' | 'mocha' | 'other';

/**
 * 购物车项目类型
 */
export interface CartItem {
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
  id: number;
  userId: number;
  orderNo: string;
  totalPrice: number;
  status: OrderStatus;
  orderType: OrderType;
  items: OrderItem[];
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
  id: number;
  orderId: number;
  coffeeId: number;
  coffeeName: string;
  coffeeImage: string;
  quantity: number;
  price: number;
  size?: string;
}

/**
 * 用户登录请求类型
 */
export interface LoginRequest {
  username: string;
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
}

/**
 * 分页请求参数
 */
export interface PageParams {
  page: number;
  pageSize: number;
}

/**
 * 分页响应类型
 */
export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
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
