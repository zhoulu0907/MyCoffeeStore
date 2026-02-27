# MyCoffeeStore 咖啡店网站

位于旧金山 Haight Ashbury 的精品咖啡店网站，支持咖啡浏览、用户注册登录、购物车、下单等功能。

## 项目概览

| 项目 | 说明 |
|------|------|
| 项目地址 | https://github.com/zhoulu0907/MyCoffeeStore.git |
| 设计稿 | `docs/design/coffee-store.pen`（Pencil MCP 设计） |
| 前端访问 | http://localhost:5173 |
| 后端 API | http://localhost:8080/api |
| Swagger 文档 | http://localhost:8080/api/swagger-ui.html |
| 数据库 | PostgreSQL 16（Docker），端口 5432 |

## 功能模块

1. 首页宣传 + 轮播图
2. 用户注册 / 登录（JWT 认证）
3. 咖啡浏览（分类筛选、搜索、分页）
4. 咖啡详情（尺寸选择、数量选择）
5. 购物车管理（增删改查）
6. 订单管理（创建、查看、取消）

## 技术栈

### 前端

| 技术 | 版本 |
|------|------|
| React | 19.2 |
| TypeScript | 5.9 |
| Vite | 7.3 |
| Tailwind CSS | 3.4 |
| React Router | 6.22 |
| Axios | 1.6 |

### 后端

| 技术 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.1.5 |
| MyBatis-Flex | 1.9.7 |
| PostgreSQL | 16 |
| JJWT | 0.12.3 |
| SpringDoc OpenAPI | 2.2.0 |

## 项目结构

```
MyCoffeeStore/
├── docs/                          # 本地文档（不提交 git）
│   ├── readme.md                  # 本文件
│   ├── design/                    # 设计稿
│   │   └── coffee-store.pen
│   ├── prd/                       # 需求文档
│   └── plan/                      # 实施计划
│
├── backend/                       # 后端项目（Spring Boot）
│   ├── pom.xml
│   └── src/main/java/com/mycoffeestore/
│       ├── CoffeeApplication.java     # 启动类
│       ├── entity/                    # 实体类（User, Coffee, Cart, Order, OrderItem）
│       ├── mapper/                    # Mapper 接口（继承 BaseMapper）
│       ├── dto/                       # 请求 DTO（auth/, cart/, order/）
│       ├── vo/                        # 响应 VO（auth/, cart/, coffee/, order/）
│       ├── controller/                # 控制器（Auth, Coffee, Cart, Order）
│       ├── service/                   # 业务层接口 + impl/
│       ├── config/                    # 配置（WebConfig, JwtInterceptor）
│       ├── common/                    # 通用类（BaseEntity, Result, PageResult）
│       ├── enums/                     # 枚举（OrderStatus, OrderType）
│       ├── exception/                 # 异常处理
│       └── util/                      # 工具类（JwtUtil）
│
└── frontend/my-coffee-store-frontend/ # 前端项目（React + Vite）
    ├── src/
    │   ├── components/            # 通用组件（Header, Footer, CoffeeCard, Carousel, Loading 等）
    │   ├── pages/                 # 页面组件（Home, CoffeeList, CoffeeDetail, Cart, Order, Login, Register）
    │   ├── contexts/              # 状态管理（AuthContext, CartContext）
    │   ├── services/              # API 服务层（api.ts）
    │   ├── types/                 # TypeScript 类型定义
    │   ├── hooks/                 # 自定义 Hooks
    │   └── utils/                 # 工具函数和常量
    ├── package.json
    ├── vite.config.ts             # Vite 配置（代理 /api -> localhost:8080）
    ├── tailwind.config.js
    └── postcss.config.js
```

## 数据库

### 表结构

| 表名 | 说明 |
|------|------|
| mcs_user | 用户表 |
| mcs_coffee | 咖啡产品表 |
| mcs_cart | 购物车表 |
| mcs_order | 订单表 |
| mcs_order_item | 订单详情表 |

### 连接信息

- 主机：localhost:5432
- 数据库：coffee_store
- 用户名：postgres
- 密码：postgres123

## 启动部署

### 1. 启动数据库（PostgreSQL Docker）

```bash
# 首次创建容器
docker run -d --name coffee-store-postgres \
  -e POSTGRES_PASSWORD=postgres123 \
  -e POSTGRES_DB=coffee_store \
  -p 5432:5432 \
  postgres:16-alpine

# 后续启动已有容器
docker start coffee-store-postgres

# 检查状态
docker ps | grep postgres
```

### 2. 启动后端（Spring Boot）

```bash
cd backend

# 编译
mvn clean compile

# 启动（运行在 http://localhost:8080/api）
mvn spring-boot:run
```

### 3. 启动前端（Vite + React）

```bash
cd frontend/my-coffee-store-frontend

# 安装依赖（首次）
npm install

# 启动开发服务器（运行在 http://localhost:5173）
npm run dev
```

### 端口占用处理

如果端口被占用，先停止已有进程：

```bash
# 查找并停止占用端口的进程
lsof -ti:5173 | xargs kill -9   # 前端
lsof -ti:8080 | xargs kill -9   # 后端
```

## API 接口

所有接口以 `/api` 为前缀，仅使用 GET 和 POST 方法。

### 认证接口（/v1/auth）

| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | /v1/auth/register | 用户注册 | 否 |
| POST | /v1/auth/login | 用户登录 | 否 |
| GET | /v1/auth/info | 获取用户信息 | 是 |
| POST | /v1/auth/logout | 退出登录 | 是 |

### 咖啡接口（/v1/coffee）

| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| GET | /v1/coffee/list | 咖啡列表（分页） | 否 |
| GET | /v1/coffee/detail | 咖啡详情 | 否 |
| GET | /v1/coffee/categories | 分类列表 | 否 |

### 购物车接口（/v1/cart）

| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| GET | /v1/cart/list | 购物车列表 | 是 |
| POST | /v1/cart/add | 添加到购物车 | 是 |
| POST | /v1/cart/update | 更新数量 | 是 |
| POST | /v1/cart/remove | 删除购物车项 | 是 |
| POST | /v1/cart/clear | 清空购物车 | 是 |

### 订单接口（/v1/order）

| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | /v1/order/create | 创建订单 | 是 |
| GET | /v1/order/list | 订单列表（分页） | 是 |
| GET | /v1/order/detail | 订单详情 | 是 |
| POST | /v1/order/cancel | 取消订单 | 是 |

## 开发注意事项

- 前端通过 Vite 代理将 `/api` 请求转发到后端 `localhost:8080`
- JWT Token 有效期 7 天，存储在 localStorage
- 后端使用 `@MapperScan("com.mycoffeestore.mapper")` 扫描 Mapper
- 类型导入使用 `import type` 语法（Vite ESM 要求）
- `docs/` 目录仅本地使用，不提交到 git
