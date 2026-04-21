# 接口定义

## 概述

本系统采用RESTful API风格，前端与后端通过JSON数据进行交互。API分为用户端API和管理端API两部分。

## 基础规范

### 认证方式
- 用户端API使用JWT Token认证
- Token放在请求头 `Authorization: Bearer <token>`
- 管理端API需要管理员权限

### 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 错误码定义
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 用户端API

### 1. 用户模块

#### 注册
- `POST /api/user/register` - 用户注册（手机号/邮箱）
- `POST /api/user/sendCode` - 发送验证码

#### 登录
- `POST /api/user/login` - 用户登录
- `POST /api/user/logout` - 用户登出
- `GET /api/user/info` - 获取当前用户信息
- `PUT /api/user/info` - 更新用户信息
- `PUT /api/user/password` - 修改密码
- `POST /api/user/resetPassword` - 找回密码

### 2. 商品模块

#### 商品浏览
- `GET /api/book/list` - 商品列表（分页+筛选）
- `GET /api/book/{id}` - 商品详情
- `GET /api/book/home` - 首页数据（热门推荐、最新上架、Feed流）
- `GET /api/book/feed` - Feed流推荐
- `GET /api/book/search` - 搜索商品

#### 商品管理
- `POST /api/book` - 发布商品
- `PUT /api/book/{id}` - 编辑商品
- `DELETE /api/book/{id}` - 删除商品
- `GET /api/book/my` - 我的发布商品
- `PUT /api/book/{id}/status` - 更新商品状态

### 3. 分类模块
- `GET /api/category/tree` - 获取分类树
- `GET /api/category/{id}/books` - 获取分类下的商品

### 4. 购物车模块
- `GET /api/cart` - 获取购物车列表
- `POST /api/cart` - 添加商品到购物车
- `PUT /api/cart/{id}` - 更新购物车商品数量
- `DELETE /api/cart/{id}` - 删除购物车商品
- `DELETE /api/cart` - 清空购物车

### 5. 订单模块
- `POST /api/order` - 创建订单
- `GET /api/order/list` - 订单列表
- `GET /api/order/{id}` - 订单详情
- `PUT /api/order/{id}/pay` - 模拟支付
- `PUT /api/order/{id}/ship` - 卖家发货
- `PUT /api/order/{id}/receive` - 买家确认收货
- `PUT /api/order/{id}/cancel` - 取消订单

### 6. 评价模块
- `POST /api/review` - 发布评价
- `GET /api/review/book/{bookId}` - 商品评价列表
- `GET /api/review/user/{userId}` - 用户收到的评价

### 7. 消息模块
- `GET /api/message/list` - 消息列表（分类）
- `GET /api/message/unread` - 未读消息数
- `PUT /api/message/{id}/read` - 标记消息已读
- `PUT /api/message/readAll` - 全部标记已读
- `POST /api/message/private` - 发送私信
- `GET /api/message/private/{userId}` - 获取与某用户的私信历史

### 8. 收藏/足迹模块
- `POST /api/favorite` - 收藏商品
- `DELETE /api/favorite/{bookId}` - 取消收藏
- `GET /api/favorite` - 收藏列表
- `POST /api/footprint` - 添加浏览足迹
- `GET /api/footprint` - 浏览足迹

### 9. 搜索模块
- `GET /api/search/history` - 搜索历史
- `DELETE /api/search/history` - 清空搜索历史
- `GET /api/search/hot` - 热门搜索
- `GET /api/search/recommend` - 搜索推荐

### 10. 举报模块
- `POST /api/report` - 提交举报/投诉

## 管理端API

### 1. 认证模块
- `POST /api/admin/login` - 管理员登录
- `GET /api/admin/info` - 获取管理员信息

### 2. 用户管理
- `GET /api/admin/user/list` - 用户列表
- `GET /api/admin/user/{id}` - 用户详情
- `PUT /api/admin/user/{id}/status` - 禁用/启用用户

### 3. 商品管理
- `GET /api/admin/book/list` - 待审核商品列表
- `PUT /api/admin/book/{id}/audit` - 审核商品
- `PUT /api/admin/book/{id}/offline` - 下架商品
- `GET /api/admin/book/stats` - 商品统计

### 4. 订单管理
- `GET /api/admin/order/list` - 订单列表
- `GET /api/admin/order/{id}` - 订单详情
- `GET /api/admin/order/stats` - 订单统计

### 5. 评价管理
- `GET /api/admin/review/list` - 评价列表
- `PUT /api/admin/review/{id}/audit` - 审核评价
- `DELETE /api/admin/review/{id}` - 删除评价

### 6. 举报投诉管理
- `GET /api/admin/report/list` - 举报列表
- `PUT /api/admin/report/{id}/handle` - 处理举报
- `GET /api/admin/feedback/list` - 反馈建议列表

### 7. 分类管理
- `GET /api/admin/category/tree` - 分类树
- `POST /api/admin/category` - 添加分类
- `PUT /api/admin/category/{id}` - 更新分类
- `DELETE /api/admin/category/{id}` - 删除分类

### 8. 标签管理
- `GET /api/admin/tag/list` - 标签列表
- `POST /api/admin/tag` - 添加标签
- `PUT /api/admin/tag/{id}` - 更新标签
- `DELETE /api/admin/tag/{id}` - 删除标签

### 9. 公告管理
- `GET /api/admin/notice/list` - 公告列表
- `POST /api/admin/notice` - 发布公告
- `PUT /api/admin/notice/{id}` - 更新公告
- `DELETE /api/admin/notice/{id}` - 删除公告

### 10. 日志管理
- `GET /api/admin/log/operation` - 操作日志
- `GET /api/admin/log/login` - 登录日志

### 11. 统计面板
- `GET /api/admin/dashboard/stats` - 概览统计
- `GET /api/admin/dashboard/chart` - 图表数据

## 实体对象

### User（用户）
```json
{
  "id": "long",
  "username": "string",
  "phone": "string",
  "email": "string",
  "avatar": "string",
  "nickname": "string",
  "role": "string",
  "creditScore": "int",
  "createTime": "datetime"
}
```

### Book（商品）
```json
{
  "id": "long",
  "title": "string",
  "description": "string",
  "price": "decimal",
  "originalPrice": "decimal",
  "images": ["string"],
  "categoryId": "long",
  "categoryName": "string",
  "condition": "string",
  "tags": ["string"],
  "status": "string",
  "userId": "long",
  "sellerNickname": "string",
  "viewCount": "int",
  "favoriteCount": "int",
  "createTime": "datetime",
  "updateTime": "datetime"
}
```

### Order（订单）
```json
{
  "id": "long",
  "orderNo": "string",
  "userId": "long",
  "totalAmount": "decimal",
  "status": "string",
  "items": [{
    "bookId": "long",
    "bookTitle": "string",
    "price": "decimal",
    "quantity": "int"
  }],
  "receiverName": "string",
  "receiverPhone": "string",
  "receiverAddress": "string",
  "payTime": "datetime",
  "shipTime": "datetime",
  "receiveTime": "datetime",
  "createTime": "datetime"
}
```

### Message（消息）
```json
{
  "id": "long",
  "type": "string",
  "title": "string",
  "content": "string",
  "fromUserId": "long",
  "toUserId": "long",
  "isRead": "boolean",
  "createTime": "datetime"
}
```
