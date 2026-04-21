# 日志快速参考

## 📝 已添加日志的关键业务

### 用户模块
- ✅ 用户注册（UserController.register）
- ✅ 用户登录（UserController.login）
- ✅ 更新用户信息（UserController.updateUserInfo）
- ✅ 修改密码（UserController.updatePassword）
- ✅ 发送验证码（UserServiceImpl.sendVerifyCode）
- ✅ 更新手机号（UserServiceImpl.updatePhone）

### 订单模块
- ✅ 创建订单（OrderController.createOrder / OrderServiceImpl.createOrder）
- ✅ 支付订单（OrderController.payOrder / OrderServiceImpl.payOrder）
- ✅ 取消订单（OrderController.cancelOrder）

### 图书模块
- ✅ 发布图书（BookServiceImpl.publish）
- ✅ 删除图书（BookServiceImpl.deleteBook）

## 🔍 查看日志命令

### Windows PowerShell
```powershell
# 实时查看所有日志
Get-Content logs\book-platform-all.log -Wait -Tail 50

# 查看业务日志
Get-Content logs\book-platform-business.log -Wait -Tail 50

# 查看错误日志
Get-Content logs\book-platform-error.log -Wait -Tail 50

# 搜索特定内容
Select-String -Path logs\book-platform-business.log -Pattern "订单"
```

### Linux/Mac
```bash
# 实时查看所有日志
tail -f logs/book-platform-all.log

# 查看业务日志
tail -f logs/book-platform-business.log

# 搜索特定内容
grep "订单" logs/book-platform-business.log
```

## 💡 在代码中添加日志

### 基础用法
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YourClass {
    private static final Logger logger = LoggerFactory.getLogger(YourClass.class);
    
    public void yourMethod() {
        logger.debug("调试信息");
        logger.info("普通信息");
        logger.warn("警告信息");
        logger.error("错误信息");
    }
}
```

### 使用LogUtils工具类
```java
import com.campus.book.util.LogUtils;

// 业务操作日志
LogUtils.logBusiness(logger, userId, "操作类型", "资源类型", "详细信息");
// 示例: LogUtils.logBusiness(logger, 1L, "创建", "订单", "订单号: ORD123");

// 访问日志
LogUtils.logAccess(logger, userId, "GET", "/api/books", "192.168.1.1");

// 异常日志
try {
    // 业务逻辑
} catch (Exception e) {
    LogUtils.logError(logger, "操作失败描述", e);
}

// 性能日志
long start = System.currentTimeMillis();
// 业务逻辑...
long duration = System.currentTimeMillis() - start;
LogUtils.logPerformance(logger, "方法名", duration);
```

## 📊 日志级别选择

| 场景 | 级别 | 示例 |
|------|------|------|
| 变量值、方法参数 | DEBUG | `logger.debug("参数: {}", param)` |
| 用户登录、订单创建 | INFO | `logger.info("用户登录成功: userId={}", id)` |
| 验证失败、权限不足 | WARN | `logger.warn("密码错误: username={}", username)` |
| 系统错误、异常 | ERROR | `logger.error("数据库连接失败", exception)` |

## 🎯 最佳实践

### ✅ 推荐
```java
// 1. 使用占位符
logger.info("用户 {} 购买了商品 {}", userId, productId);

// 2. 记录关键业务节点
logger.info("订单创建成功: orderId={}, amount={}", orderId, amount);

// 3. 异常时记录完整堆栈
logger.error("操作失败", exception);

// 4. 敏感信息脱敏
logger.info("用户手机号: {}", phone.substring(0, 3) + "****" + phone.substring(7));
```

### ❌ 避免
```java
// 1. 字符串拼接
logger.info("用户" + userId + "购买了商品" + productId);  // ❌

// 2. 记录敏感信息
logger.info("密码: {}", password);  // ❌ 绝对禁止

// 3. 吞掉异常
logger.error("出错了");  // ❌ 没有记录异常对象

// 4. 循环中大量日志
for (Item item : items) {
    logger.info("处理: {}", item);  // ❌ 数据量大时影响性能
}
```

## 🔧 自动日志（AOP）

所有Controller方法**自动记录**，无需手动添加：
- ✅ 请求URI和方法
- ✅ 客户端IP
- ✅ 请求参数
- ✅ 响应结果
- ✅ 执行时间
- ✅ 异常信息

日志自动写入：
- `book-platform-access.log`
- `book-platform-business.log`

## 📁 日志文件说明

| 文件名 | 内容 | 用途 |
|--------|------|------|
| book-platform-all.log | 所有级别 | 完整问题排查 |
| book-platform-info.log | INFO及以上 | 正常运行状态 |
| book-platform-error.log | ERROR级别 | 快速定位错误 |
| book-platform-business.log | 业务操作 | 追踪用户操作 |
| book-platform-access.log | HTTP访问 | 分析访问行为 |

## ⚙️ 调整日志级别

编辑 `application.yml`:
```yaml
logging:
  level:
    com.campus.book: DEBUG  # 改为 INFO/WARN/ERROR
```

编辑 `logback-spring.xml`:
```xml
<!-- 修改特定包的日志级别 -->
<logger name="com.campus.book.service" level="DEBUG"/>
```

## 🚀 快速测试

启动项目后访问：
```bash
# 测试用户登录
POST http://localhost:8080/api/user/login
{
  "username": "admin",
  "password": "123456abc"
}

# 然后查看日志
Get-Content logs\book-platform-business.log -Wait -Tail 20
```

## 📖 更多文档

- **详细配置**: LOG_INTEGRATION_SUMMARY.md
- **使用指南**: LOG_USAGE.md
- **快速开始**: QUICK_START_LOG.md
- **目录说明**: logs/README.md
