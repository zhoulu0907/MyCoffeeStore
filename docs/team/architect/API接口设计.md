# MyCoffeeStore API 接口设计

> 设计原则：仅使用 GET 和 POST 方法
> - GET：所有查询操作
> - POST：所有写操作（创建、更新、删除）

## 基础信息

### 统一响应格式

所有接口返回统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1707206400000
}
```

### 状态码定义

| code | message | 说明 |
|------|---------|------|
| 200 | 操作成功 | 请求处理成功 |
| 400 | 请求参数错误 | 参数校验失败 |
| 401 | 未登录或登录过期 | 需要重新登录 |
| 403 | 无权限 | 没有操作权限 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 500 | 服务器错误 | 服务器内部错误 |

### 统一前缀

```
/api/v1
```

---

## 1. 用户认证模块

### 1.1 用户注册

**接口地址**：`POST /api/v1/auth/register`

**请求参数**：
```json
{
  "username": "string",  // 用户名，必填，3-20字符
  "password": "string",  // 密码，必填，6-20字符
  "email": "string",     // 邮箱，必填，格式校验
  "phone": "string"      // 手机号，选填，格式校验
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "coffee_lover",
    "email": "coffee@example.com",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "timestamp": 1707206400000
}
```

---

### 1.2 用户登录

**接口地址**：`POST /api/v1/auth/login`

**请求参数**：
```json
{
  "account": "string",  // 账号（用户名/邮箱/手机号），必填
  "password": "string"  // 密码，必填
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "coffee_lover",
    "email": "coffee@example.com",
    "phone": "14155551234",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "timestamp": 1707206400000
}
```

---

### 1.3 获取当前用户信息

**接口地址**：`GET /api/v1/auth/info`

**请求头**：
```
Authorization: Bearer {token}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "userId": 1,
    "username": "coffee_lover",
    "email": "coffee@example.com",
    "phone": "14155551234",
    "createTime": "2024-02-26T10:00:00"
  },
  "timestamp": 1707206400000
}
```

---

### 1.4 退出登录

**接口地址**：`POST /api/v1/auth/logout`

**请求头**：
```
Authorization: Bearer {token}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null,
  "timestamp": 1707206400000
}
```

---

## 2. 咖啡产品模块

### 2.1 获取咖啡列表

**接口地址**：`GET /api/v1/coffee/list`

**请求参数**（Query String）：
```
category: string  // 分类筛选（可选），如：espresso, brew, cold
page: int         // 页码，默认1
size: int         // 每页数量，默认10
```

**响应数据**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 25,
    "page": 1,
    "size": 10,
    "list": [
      {
        "coffeeId": 1,
        "name": "经典美式",
        "description": "精选阿拉比卡豆，深度烘焙",
        "price": 4.50,
        "category": "espresso",
        "imageUrl": "https://cdn.example.com/coffee/americano.jpg",
        "stock": 100,
        "status": 1
      }
    ]
  },
  "timestamp": 1707206400000
}
```

---

### 2.2 获取咖啡详情

**接口地址**：`GET /api/v1/coffee/detail`

**请求参数**（Query String）：
```
coffeeId: long  // 咖啡ID，必填
```

**响应数据**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "coffeeId": 1,
    "name": "经典美式",
    "description": "精选阿拉比卡豆，深度烘焙，口感醇厚",
    "price": 4.50,
    "originalPrice": 5.50,
    "category": "espresso",
    "categoryName": "意式浓缩系列",
    "imageUrl": "https://cdn.example.com/coffee/americano.jpg",
    "images": [
      "https://cdn.example.com/coffee/americano-1.jpg",
      "https://cdn.example.com/coffee/americano-2.jpg"
    ],
    "stock": 100,
    "status": 1,
    "sales": 1250,
    "createTime": "2024-02-01T10:00:00"
  },
  "timestamp": 1707206400000
}
```

---

### 2.3 获取咖啡分类

**接口地址**：`GET /api/v1/coffee/categories`

**响应数据**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "code": "espresso",
      "name": "意式浓缩系列",
      "count": 8
    },
    {
      "code": "brew",
      "name": "手冲系列",
      "count": 12
    },
    {
      "code": "cold",
      "name": "冷萃/冰咖啡",
      "count": 5
    }
  ],
  "timestamp": 1707206400000
}
```

---

## 3. 购物车模块

### 3.1 添加到购物车

**接口地址**：`POST /api/v1/cart/add`

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "coffeeId": 1,      // 咖啡ID，必填
  "quantity": 2       // 数量，必填，大于0
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "cartId": 101,
    "coffeeId": 1,
    "quantity": 2,
    "subtotal": 9.00
  },
  "timestamp": 1707206400000
}
```

---

### 3.2 从购物车移除

**接口地址**：`POST /api/v1/cart/remove`

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "cartId": 101  // 购物车项ID，必填
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "移除成功",
  "data": null,
  "timestamp": 1707206400000
}
```

---

### 3.3 更新购物车数量

**接口地址**：`POST /api/v1/cart/update`

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "cartId": 101,      // 购物车项ID，必填
  "quantity": 3       // 新数量，必填，大于0
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "cartId": 101,
    "quantity": 3,
    "subtotal": 13.50
  },
  "timestamp": 1707206400000
}
```

---

### 3.4 获取购物车列表

**接口地址**：`GET /api/v1/cart/list`

**请求头**：
```
Authorization: Bearer {token}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalQuantity": 5,
    "totalPrice": 22.50,
    "items": [
      {
        "cartId": 101,
        "coffeeId": 1,
        "coffeeName": "经典美式",
        "imageUrl": "https://cdn.example.com/coffee/americano.jpg",
        "price": 4.50,
        "quantity": 2,
        "subtotal": 9.00,
        "stock": 100,
        "status": 1
      },
      {
        "cartId": 102,
        "coffeeId": 3,
        "coffeeName": "卡布奇诺",
        "imageUrl": "https://cdn.example.com/coffee/cappuccino.jpg",
        "price": 5.50,
        "quantity": 1,
        "subtotal": 5.50,
        "stock": 50,
        "status": 1
      }
    ]
  },
  "timestamp": 1707206400000
}
```

---

### 3.5 清空购物车

**接口地址**：`POST /api/v1/cart/clear`

**请求头**：
```
Authorization: Bearer {token}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "清空成功",
  "data": null,
  "timestamp": 1707206400000
}
```

---

## 4. 订单模块

### 4.1 创建订单

**接口地址**：`POST /api/v1/order/create`

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "orderType": "dine_in",           // 订单类型，必填：dine_in(堂食)/takeaway(外带)/delivery(外卖)
  "items": [                         // 订单项，必填
    {
      "coffeeId": 1,
      "quantity": 2,
      "price": 4.50
    },
    {
      "coffeeId": 3,
      "quantity": 1,
      "price": 5.50
    }
  ],
  "remark": "少糖，少冰",            // 备注，选填
  "deliveryAddress": {               // 外卖地址，orderType=delivery时必填
    "address": "123 Haight St",
    "city": "San Francisco",
    "state": "CA",
    "zipCode": "94117",
    "phone": "14155551234"
  }
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "orderId": "ORD20240226001",
    "totalAmount": 14.50,
    "orderType": "dine_in",
    "status": "pending",
    "createTime": "2024-02-26T10:30:00"
  },
  "timestamp": 1707206400000
}
```

---

### 4.2 获取订单详情

**接口地址**：`GET /api/v1/order/detail`

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**（Query String）：
```
orderId: string  // 订单号，必填
```

**响应数据**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "orderId": "ORD20240226001",
    "userId": 1,
    "username": "coffee_lover",
    "totalAmount": 14.50,
    "orderType": "dine_in",
    "orderTypeName": "堂食",
    "status": "preparing",
    "statusName": "制作中",
    "remark": "少糖，少冰",
    "items": [
      {
        "itemId": 1,
        "coffeeId": 1,
        "coffeeName": "经典美式",
        "imageUrl": "https://cdn.example.com/coffee/americano.jpg",
        "quantity": 2,
        "price": 4.50,
        "subtotal": 9.00
      },
      {
        "itemId": 2,
        "coffeeId": 3,
        "coffeeName": "卡布奇诺",
        "imageUrl": "https://cdn.example.com/coffee/cappuccino.jpg",
        "quantity": 1,
        "price": 5.50,
        "subtotal": 5.50
      }
    ],
    "createTime": "2024-02-26T10:30:00",
    "updateTime": "2024-02-26T10:31:00"
  },
  "timestamp": 1707206400000
}
```

---

### 4.3 获取用户订单列表

**接口地址**：`GET /api/v1/order/list`

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**（Query String）：
```
status: string  // 状态筛选（可选）
page: int       // 页码，默认1
size: int       // 每页数量，默认10
```

**响应数据**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 15,
    "page": 1,
    "size": 10,
    "list": [
      {
        "orderId": "ORD20240226001",
        "totalAmount": 14.50,
        "orderType": "dine_in",
        "orderTypeName": "堂食",
        "status": "preparing",
        "statusName": "制作中",
        "itemCount": 3,
        "createTime": "2024-02-26T10:30:00"
      }
    ]
  },
  "timestamp": 1707206400000
}
```

---

### 4.4 取消订单

**接口地址**：`POST /api/v1/order/cancel`

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "orderId": "ORD20240226001",  // 订单号，必填
  "reason": "不想买了"          // 取消原因，选填
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "订单已取消",
  "data": {
    "orderId": "ORD20240226001",
    "status": "cancelled",
    "statusName": "已取消"
  },
  "timestamp": 1707206400000
}
```

---

### 4.5 更新订单状态（管理员）

**接口地址**：`POST /api/v1/order/update-status`

**请求头**：
```
Authorization: Bearer {admin_token}
```

**请求参数**：
```json
{
  "orderId": "ORD20240226001",  // 订单号，必填
  "status": "completed"         // 新状态，必填
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": {
    "orderId": "ORD20240226001",
    "status": "completed",
    "statusName": "已完成"
  },
  "timestamp": 1707206400000
}
```

---

## 5. 枚举值定义

### 5.1 订单类型 (OrderType)

| code | name | 说明 |
|------|------|------|
| dine_in | 堂食 | 在店内享用 |
| takeaway | 外带 | 打包带走 |
| delivery | 外卖 | 配送到家 |

### 5.2 订单状态 (OrderStatus)

| code | name | 说明 |
|------|------|------|
| pending | 待确认 | 订单已创建，等待商家确认 |
| confirmed | 已确认 | 商家已确认订单 |
| preparing | 制作中 | 咖啡正在制作 |
| ready | 待取餐 | 咖啡已完成，等待取餐 |
| completed | 已完成 | 订单已完成 |
| cancelled | 已取消 | 订单已取消 |

### 5.3 咖啡分类 (CoffeeCategory)

| code | name | 说明 |
|------|------|------|
| espresso | 意式浓缩系列 | 基于意式浓缩的咖啡 |
| brew | 手冲系列 | 手冲、滴滤咖啡 |
| cold | 冷萃/冰咖啡 | 冷萃及冰咖啡系列 |
| blend | 拼配豆 | 各种拼配咖啡豆 |

### 5.4 咖啡状态 (CoffeeStatus)

| code | name | 说明 |
|------|------|------|
| 0 | 下架 | 暂不销售 |
| 1 | 上架 | 正常销售 |
| 2 | 售罄 | 暂时缺货 |

---

## 6. 错误码定义

| code | message | 场景 |
|------|---------|------|
| 1001 | 用户名已存在 | 注册时用户名重复 |
| 1002 | 邮箱已注册 | 注册时邮箱重复 |
| 1003 | 用户名或密码错误 | 登录凭证错误 |
| 1004 | 用户已被禁用 | 账号被管理员禁用 |
| 2001 | 咖啡不存在 | 查询的咖啡ID无效 |
| 2002 | 咖啡库存不足 | 购买数量超过库存 |
| 2003 | 咖啡已下架 | 尝试购买已下架商品 |
| 3001 | 购物车为空 | 结算时购物车无商品 |
| 3002 | 购物车项不存在 | 操作的购物车项无效 |
| 4001 | 订单不存在 | 查询的订单无效 |
| 4002 | 订单状态不允许操作 | 当前状态下无法执行操作 |
| 4003 | 订单已过期 | 订单超时未支付 |
| 5001 | 未登录 | Token缺失或无效 |
| 5002 | Token已过期 | 需要重新登录 |
| 5003 | 无权限 | 权限不足 |

---

## 7. 接口使用示例

### 7.1 用户注册并下单完整流程

```bash
# 1. 注册
POST /api/v1/auth/register
{
  "username": "coffee_lover",
  "password": "Coffee123!",
  "email": "coffee@example.com"
}

# 2. 登录
POST /api/v1/auth/login
{
  "account": "coffee_lover",
  "password": "Coffee123!"
}
# 返回 token，保存到本地

# 3. 查看咖啡列表
GET /api/v1/coffee/list?category=espresso&page=1&size=10

# 4. 添加到购物车
POST /api/v1/cart/add
Headers: Authorization: Bearer {token}
{
  "coffeeId": 1,
  "quantity": 2
}

# 5. 查看购物车
GET /api/v1/cart/list
Headers: Authorization: Bearer {token}

# 6. 创建订单
POST /api/v1/order/create
Headers: Authorization: Bearer {token}
{
  "orderType": "dine_in",
  "items": [
    {"coffeeId": 1, "quantity": 2, "price": 4.50}
  ]
}

# 7. 查询订单
GET /api/v1/order/detail?orderId=ORD20240226001
Headers: Authorization: Bearer {token}
```

---

## 8. 注意事项

1. **时间格式**：所有时间字段使用 ISO 8601 格式 (yyyy-MM-dd'T'HH:mm:ss)
2. **价格精度**：所有价格使用 DECIMAL 类型，保留两位小数
3. **分页参数**：page 从 1 开始，size 默认为 10，最大 100
4. **Token 有效期**：默认 7 天，过期后需重新登录
5. **频率限制**：同一用户 1 分钟内最多 60 次请求
6. **图片大小限制**：上传图片最大 5MB

---

*文档创建时间：2024-02-26*
*文档版本：v1.0*
