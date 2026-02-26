# MyCoffeeStore 后端服务

MyCoffeeStore 咖啡店后端服务，基于 Spring Boot 3.x + MyBatis-Flex + MySQL 构建。

## 技术栈

- **Java**: 17+
- **Spring Boot**: 3.2.0
- **MyBatis-Flex**: 1.7.8
- **MySQL**: 8.0+
- **JWT**: JJWT 0.12.3
- **API 文档**: SpringDoc OpenAPI 3

## 项目结构

```
src/main/java/com/mycoffeestore/
├── CoffeeApplication.java    # 启动类
├── config/                   # 配置类
│   ├── WebConfig.java       # Web配置、CORS
│   └── JwtInterceptor.java  # JWT拦截器
├── controller/               # 控制器
│   ├── AuthController.java  # 用户认证
│   ├── CoffeeController.java # 咖啡产品
│   ├── CartController.java  # 购物车
│   └── OrderController.java # 订单
├── service/                  # 服务层
│   ├── auth/               # 认证服务
│   ├── coffee/             # 咖啡服务
│   ├── cart/               # 购物车服务
│   └── order/              # 订单服务
├── mapper/                   # 数据访问层
│   ├── UserMapper.java
│   ├── CoffeeMapper.java
│   ├── CartMapper.java
│   ├── OrderMapper.java
│   └── OrderItemMapper.java
├── entity/                   # 数据库实体
│   ├── User.java
│   ├── Coffee.java
│   ├── Cart.java
│   ├── Order.java
│   └── OrderItem.java
├── dto/                      # 数据传输对象
│   ├── auth/               # 认证相关DTO
│   ├── cart/               # 购物车DTO
│   └── order/              # 订单DTO
├── vo/                       # 视图对象
│   ├── auth/               # 认证相关VO
│   ├── coffee/             # 咖啡VO
│   ├── cart/               # 购物车VO
│   └── order/              # 订单VO
├── common/                   # 通用类
│   ├── result/             # 统一响应格式
│   ├── base/               # 基础类
│   ├── entity/             # 通用实体
│   └── query/              # 查询基类
├── enums/                   # 枚举类
│   ├── OrderType.java
│   └── OrderStatus.java
├── util/                    # 工具类
│   └── JwtUtil.java
└── exception/               # 异常处理
    ├── BusinessException.java
    └── GlobalExceptionHandler.java
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化

执行 `src/main/resources/schema.sql` 创建数据库和表：

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 3. 配置修改

修改 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/coffee_store?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 4. 启动项目

```bash
mvn clean install
mvn spring-boot:run
```

或直接运行主类 `CoffeeApplication.java`

### 5. 访问 API 文档

启动后访问 Swagger UI: http://localhost:8080/api/swagger-ui.html

## API 接口

### 用户认证

- `POST /api/v1/auth/register` - 用户注册
- `POST /api/v1/auth/login` - 用户登录
- `GET /api/v1/auth/info` - 获取用户信息
- `POST /api/v1/auth/logout` - 用户退出

### 咖啡产品

- `GET /api/v1/coffee/list` - 获取咖啡列表
- `GET /api/v1/coffee/detail` - 获取咖啡详情
- `GET /api/v1/coffee/categories` - 获取咖啡分类

### 购物车

- `POST /api/v1/cart/add` - 添加到购物车
- `POST /api/v1/cart/remove` - 从购物车移除
- `POST /api/v1/cart/update` - 更新购物车数量
- `GET /api/v1/cart/list` - 获取购物车列表
- `POST /api/v1/cart/clear` - 清空购物车

### 订单管理

- `POST /api/v1/order/create` - 创建订单
- `GET /api/v1/order/detail` - 获取订单详情
- `GET /api/v1/order/list` - 获取订单列表
- `POST /api/v1/order/cancel` - 取消订单
- `POST /api/v1/order/update-status` - 更新订单状态（管理员）

## 测试账号

- 用户名: `test_user`
- 密码: `Coffee123!`

## 开发规范

1. **仅使用 GET 和 POST 方法**
2. **所有 VO/DO/DTO 类必须包含 @Schema 注解**
3. **中文注释**
4. **统一异常处理**
5. **统一响应格式 Result<T>**

## 构建

```bash
mvn clean package
```

生成的 JAR 文件位于 `target/my-coffee-store-backend-1.0.0.jar`

## License

Copyright © 2024 MyCoffeeStore
