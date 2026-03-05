# MyCoffeeStore 前端项目

基于 React 18 + Vite + TypeScript 开发的咖啡店网站前端项目。

## 技术栈

- **框架**: React 18
- **构建工具**: Vite
- **语言**: TypeScript
- **路由**: React Router v6
- **状态管理**: Context API
- **HTTP 客户端**: Axios
- **样式**: Tailwind CSS

## 项目结构

```
src/
├── components/      # 通用组件
│   ├── Header.tsx
│   ├── Footer.tsx
│   ├── CoffeeCard.tsx
│   ├── Carousel.tsx
│   ├── Loading.tsx
│   ├── ProtectedRoute.tsx  # 路由权限控制
│   └── admin/           # 管理员组件
│       ├── AdminHeader.tsx
│       ├── UsersSection.tsx
│       ├── UserRow.tsx
│       ├── LlmConfigSection.tsx
│       ├── ConfigField.tsx
│       └── index.ts
├── pages/          # 页面组件
│   ├── Home.tsx
│   ├── Login.tsx
│   ├── Register.tsx
│   ├── CoffeeList.tsx
│   ├── CoffeeDetail.tsx
│   ├── Cart.tsx
│   ├── Order.tsx
│   ├── Profile.tsx
│   └── AdminPage.tsx     # 管理员页面
├── contexts/       # Context 状态管理
│   ├── AuthContext.tsx
│   └── CartContext.tsx
├── services/       # API 服务
│   └── api.ts
├── hooks/          # 自定义 Hooks
│   ├── useLocalStorage.ts
│   ├── useDebounce.ts
│   └── useFetch.ts
├── types/          # TypeScript 类型
│   ├── index.ts
│   └── admin.ts        # 管理员相关类型
├── utils/          # 工具函数
│   ├── constants.ts
│   └── helpers.ts
├── App.tsx         # 应用根组件
└── main.tsx        # 应用入口
```

## 管理员页面

### 访问路径
- **URL**: `/admin`
- **权限**: 需要管理员权限（`user.role === 'admin'`）

### 功能模块

#### 1. 用户列表
- 显示所有注册用户信息
- 包含：账户名、邮箱、订单数、最近订单
- 支持分页加载

#### 2. LLM 配置
- 配置 LLM 服务连接参数
- 支持字段：
  - Base URL: API 基础地址
  - API Key: 认证密钥
  - Model: 模型名称
  - Temperature: 温度参数 (0-2)
  - Max Tokens: 最大令牌数
- 功能：
  - 保存配置
  - 测试连接

### API 接口

管理员相关 API 定义在 `src/services/api.ts` 中的 `adminApi`：

```typescript
// 获取用户列表
adminApi.getUsers({ page: 1, size: 50 })

// 获取用户详情
adminApi.getUserDetail(userId)

// 更新用户状态
adminApi.updateUserStatus(userId, 'active' | 'inactive' | 'banned')

// 获取 LLM 配置
adminApi.getLlmConfigs()

// 更新 LLM 配置
adminApi.updateLlmConfig(config)

// 测试 LLM 连接
adminApi.testLlmConnection(provider)

// 获取可用的 LLM 提供商
adminApi.getLlmProviders()
```

### 类型定义

管理员相关类型定义在 `src/types/admin.ts`：

- `LlmConfig`: LLM 配置类型
- `LlmProvider`: LLM 提供商类型
- `AdminUser`: 管理员视图用户类型
- `UserListResponse`: 用户列表响应类型
- `LlmConfigResponse`: LLM 配置响应类型
- `TestConnectionResponse`: 测试连接响应类型

## 功能特性

### 用户功能
- 用户注册/登录
- 个人信息管理
- 订单历史查询

### 咖啡产品
- 咖啡列表展示
- 分类筛选
- 搜索功能
- 咖啡详情查看

### 购物车
- 添加/删除商品
- 数量调整
- 规格选择（尺寸）
- 本地持久化

### 订单系统
- 创建订单
- 订单状态跟踪
- 订单历史
- 订单筛选

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 环境变量

创建 `.env.local` 文件配置本地环境变量：

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## 代码规范

- 使用函数式组件
- TypeScript 类型定义完整
- 遵循 ESLint 规则
- 使用 Prettier 格式化

## 配色方案

- **主色调**: #1A1A1A (深黑)
- **强调色**: #D4A574 (金棕色)
- **背景色**: #FFFFFF (白色)
- **表面色**: #F5F5F5 (浅灰)

## 字体

- **衬线字体**: Georgia (标题)
- **无衬线字体**: Inter (正文)

## 浏览器支持

- Chrome (最新版)
- Safari (最新版)
- Firefox (最新版)
- Edge (最新版)

## 许可证

MIT
