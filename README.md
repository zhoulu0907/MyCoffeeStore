# MyCoffeeStore 咖啡店网站

位于旧金山 Haight Ashbury 的精品咖啡店网站，支持咖啡浏览、用户注册登录、购物车、下单、AI推荐等功能。

## 项目概览

| 项目 | 说明 |
|------|------|
| 项目地址 | https://github.com/zhoulu0907/MyCoffeeStore.git |
| 设计稿 | `docs/design/coffee-store.pen`（Pencil MCP 设计） |
| 前端访问 | http://localhost:5173 |
| 后端 API | http://localhost:8080/api |
| Swagger 文档 | http://localhost:8080/api/swagger-ui.html |
| 数据库 | PostgreSQL 16（Docker），端口 5432 |
| 项目状态 | ✅ 开发完成，已合并到 main 分支 |

## 最新更新 (2026-02-27)

### 已完成功能 (12个开发任务)

| # | 任务 | 状态 |
|---|------|------|
| 1 | 咖啡店网站设计更新 | ✅ 完成 |
| 2 | 创建咖啡推荐 VO 类 | ✅ 完成 |
| 3 | 创建咖啡推荐 DTO 类 | ✅ 完成 |
| 4 | 实现咖啡推荐服务类 | ✅ 完成 |
| 5 | 创建推荐控制器 | ✅ 完成 |
| 6 | 阶段3：集成测试与QA验收 | ✅ 完成 |
| 7 | 修复CoffeeGuide响应式等 | ✅ 完成 |
| 8 | 触摸目标尺寸优化 | ✅ 完成 |
| 9 | 咖啡店网站完整更新 | ✅ 完成 |
| 10 | 合并feature分支、验证功能 | ✅ 完成 |
| 11 | CoffeeContext状态管理 | ✅ 完成 |
| 12 | 咖啡店网站设计更新 | ✅ 完成 |

### 新增功能

- **AI咖啡推荐系统**: 基于角色选择（咖啡新手、上班提神、手冲玩家）的智能推荐
- **CoffeeGuide组件**: 悬浮对话框，支持展开/收起，实时AI交互
- **用户中心**: 余额查询、历史订单查看
- **结算页面**: 支持堂食/外带选择，余额支付
- **订单取消退款**: 自动退还余额到用户账户

## 功能模块

1. 首页宣传 + 三列轮播推荐
2. 用户注册 / 登录（JWT 认证）
3. 咖啡浏览（分类筛选、搜索、分页）
4. 咖啡详情（尺寸选择、数量选择）
5. 购物车管理（增删改查）
6. 结算页面（订单类型、余额支付）
7. 订单管理（创建、查看、取消、退款）
8. **AI咖啡推荐**（角色选择、实时推荐）
9. **用户中心**（余额查询、订单历史）

## 技术栈

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| React | 19.2.0 | UI框架 |
| TypeScript | 5.9.3 | 类型系统 |
| Vite | 7.3.1 | 构建工具 |
| Tailwind CSS | 3.4.1 | 样式框架 |
| React Router | 6.22.0 | 路由管理 |
| Axios | 1.6.7 | HTTP客户端 |

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.1.5 | 应用框架 |
| MyBatis-Flex | 1.9.7 | ORM框架 |
| PostgreSQL | 16 | 数据库 |
| JJWT | 0.12.3 | JWT认证 |
| SpringDoc OpenAPI | 2.2.0 | API文档 |

## 设计规范

| 元素 | 值 | 应用场景 |
|------|-----|----------|
| --background | #F7F1E8 | 页面背景 |
| --primary-dark | #1F130F | Header背景 |
| --primary | #2A1A15 | 主色调、按钮 |
| --accent | #D4A574 | 强调色 |
| --gold | #DCCCB9 | 辅助金色 |
| --surface | #F1E7DB | 卡片背景 |
| --accent-light | #EADBC9 | 浅色按钮 |
| 触摸目标 | ≥44px | 移动端交互 |
| 断点md | 768px | 平板适配 |

## 项目结构

```
MyCoffeeStore/
├── docs/                          # 文档目录
│   ├── readme.md                  # 项目说明
│   ├── design/                    # 设计稿
│   │   └── coffee-store.pen       # Pencil MCP设计文件
│   ├── prd/                       # 需求文档
│   ├── plan/                      # 实施计划
│   ├── team/                      # 团队文档
│   │   ├── architect/             # 架构设计
│   │   ├── testing/               # 测试文档
│   │   └── qa/                    # QA文档
│   └── deployment/                # 部署文档
│       ├── backend-deployment.md  # 后端部署指南
│       ├── frontend-deployment.md # 前端部署指南
│       ├── database-setup.md      # 数据库配置
│       └── docker-deployment.md   # Docker部署
│
├── backend/                       # 后端项目（Spring Boot）
│   ├── pom.xml
│   └── src/main/java/com/mycoffeestore/
│       ├── CoffeeApplication.java     # 启动类
│       ├── entity/                    # 实体类
│       ├── mapper/                    # Mapper 接口
│       ├── dto/                       # 请求 DTO
│       │   ├── auth/
│       │   ├── cart/
│       │   ├── order/
│       │   ├── user/                  # 用户相关DTO
│       │   └── coffee/                # 咖啡推荐DTO
│       ├── vo/                        # 响应 VO
│       │   ├── auth/
│       │   ├── cart/
│       │   ├── coffee/
│       │   ├── order/
│       │   └── user/                  # 用户相关VO
│       ├── controller/                # 控制器
│       │   ├── AuthController.java
│       │   ├── CoffeeController.java
│       │   ├── CartController.java
│       │   ├── OrderController.java
│       │   ├── UserController.java    # 用户控制器
│       │   ├── DataController.java    # 数据管理
│       │   └── RecommendationController.java  # AI推荐
│       ├── service/                   # 业务层
│       │   ├── auth/
│       │   ├── cart/
│       │   ├── coffee/
│       │   │   └── CoffeeRecommendationService.java  # 推荐服务
│       │   ├── order/
│       │   └── user/
│       ├── config/                    # 配置
│       ├── common/                    # 通用类
│       ├── enums/                     # 枚举
│       ├── exception/                 # 异常处理
│       └── util/                      # 工具类
│           ├── JwtUtil.java
│           └── OrderDataGenerator.java  # 测试数据生成
│
└── frontend/my-coffee-store-frontend/ # 前端项目（React + Vite）
    ├── src/
    │   ├── components/            # 通用组件
    │   │   ├── Header.tsx         # 页头（含咖啡向导按钮）
    │   │   ├── Footer.tsx         # 页脚
    │   │   ├── CoffeeCard.tsx     # 咖啡卡片
    │   │   ├── Carousel.tsx       # 轮播组件
    │   │   └── CoffeeGuide.tsx    # AI推荐对话框 ⭐
    │   ├── pages/                 # 页面组件
    │   │   ├── Home.tsx           # 首页
    │   │   ├── CoffeeList.tsx     # 咖啡列表
    │   │   ├── CoffeeDetail.tsx   # 咖啡详情
    │   │   ├── Cart.tsx           # 购物车
    │   │   ├── Checkout.tsx       # 结算页面 ⭐
    │   │   ├── Order.tsx          # 订单列表
    │   │   ├── Profile.tsx        # 用户中心 ⭐
    │   │   ├── Login.tsx          # 登录
    │   │   └── Register.tsx       # 注册
    │   ├── contexts/              # 状态管理
    │   │   ├── AuthContext.tsx    # 认证状态
    │   │   ├── CartContext.tsx    # 购物车状态
    │   │   └── CoffeeGuideContext.tsx  # 咖啡向导状态 ⭐
    │   ├── services/              # API 服务层
    │   │   └── api.ts             # API接口（含推荐API）
    │   ├── types/                 # TypeScript 类型
    │   ├── hooks/                 # 自定义 Hooks
    │   └── utils/                 # 工具函数
    ├── package.json
    ├── vite.config.ts
    ├── tailwind.config.js         # 咖啡主题色板
    └── postcss.config.js
```

## 数据库

### 表结构

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| mcs_user | 用户表 | id, username, password, balance |
| mcs_coffee | 咖啡产品表 | id, name, price, category, image_url |
| mcs_cart | 购物车表 | id, user_id, coffee_id, quantity |
| mcs_order | 订单表 | id, user_id, total_amount, status, order_type |
| mcs_order_item | 订单详情表 | id, order_id, coffee_id, quantity, price |

### 连接信息

- 主机：localhost:5432
- 数据库：coffee_store
- 用户名：postgres
- 密码：postgres123

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
| POST | /v1/order/cancel | 取消订单（退款） | 是 |

### 用户接口（/v1/user）

| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| GET | /v1/user/balance | 查询余额 | 是 |

### AI推荐接口（/v1/recommendation）⭐

| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | /v1/recommendation | 获取AI咖啡推荐 | 否 |

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

# 生产构建
npm run build
```

### 端口占用处理

如果端口被占用，先停止已有进程：

```bash
# 查找并停止占用端口的进程
lsof -ti:5173 | xargs kill -9   # 前端
lsof -ti:8080 | xargs kill -9   # 后端
```

## 部署文档

详细部署指南请参考 `docs/deployment/` 目录：

- [部署指南索引](./docs/deployment/README.md)
- [后端部署](./docs/deployment/backend-deployment.md)
- [前端部署](./docs/deployment/frontend-deployment.md)
- [数据库配置](./docs/deployment/database-setup.md)
- [Docker部署](./docs/deployment/docker-deployment.md)

## 开发注意事项

- 前端通过 Vite 代理将 `/api` 请求转发到后端 `localhost:8080`
- JWT Token 有效期 7 天，存储在 localStorage
- 后端使用 `@MapperScan("com.mycoffeestore.mapper")` 扫描 Mapper
- 类型导入使用 `import type` 语法（Vite ESM 要求）
- CoffeeGuide组件使用Context进行全局状态管理
- 所有触摸目标尺寸≥44px，符合移动端规范
- 响应式断点使用md(768px)进行平板适配

## Git 工作流

```bash
# 创建功能分支
git checkout -b feature/your-feature-name

# 提交更改
git add .
git commit -m "feat: 你的功能描述"

# 推送并创建PR
git push origin feature/your-feature-name
gh pr create --title "功能标题" --body "功能描述"

# 合并后删除分支
git branch -d feature/your-feature-name
```

## 许可证

本项目采用 MIT 许可证。
