# 日志使用指南

## 1. 基本用法

### 在类中使用日志

```java
import com.campus.book.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BookService {
    
    // 方式1: 传统方式
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    
    // 方式2: 使用工具类
    private static final Logger logger = LogUtils.getLogger(BookService.class);
    
    public void example() {
        logger.debug("调试信息");
        logger.info("普通信息");
        logger.warn("警告信息");
        logger.error("错误信息");
    }
}
```

## 2. 使用LogUtils工具类

### 记录业务日志

```java
@RestController
@RequestMapping("/books")
public class BookController {
    
    private static final Logger logger = LogUtils.getLogger(BookController.class);
    
    @PostMapping
    public Result<Book> createBook(@RequestBody BookDTO bookDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 记录业务操作
        LogUtils.logBusiness(logger, userId, "创建", "图书", 
            String.format("书名: %s, 价格: %.2f", bookDTO.getTitle(), bookDTO.getPrice()));
        
        // 业务逻辑...
        return Result.success(book);
    }
}
```

### 记录访问日志

```java
@GetMapping("/{id}")
public Result<BookVO> getBook(@PathVariable Long id) {
    HttpServletRequest request = ((ServletRequestAttributes) 
        RequestContextHolder.getRequestAttributes()).getRequest();
    Long userId = SecurityUtils.getCurrentUserId();
    
    // 记录访问日志
    LogUtils.logAccess(logger, userId, request.getMethod(), 
        request.getRequestURI(), getClientIp(request));
    
    // 业务逻辑...
    return Result.success(bookVO);
}
```

### 记录异常日志

```java
try {
    // 业务逻辑
    orderService.createOrder(orderDTO);
} catch (Exception e) {
    LogUtils.logError(logger, "创建订单失败", e);
    return Result.error("订单创建失败");
}
```

### 记录性能日志

```java
public List<BookVO> searchBooks(String keyword) {
    long startTime = System.currentTimeMillis();
    
    try {
        // 搜索逻辑
        return bookMapper.searchBooks(keyword);
    } finally {
        long duration = System.currentTimeMillis() - startTime;
        LogUtils.logPerformance(logger, "searchBooks", duration);
    }
}
```

## 3. 日志级别说明

| 级别 | 用途 | 示例 |
|------|------|------|
| DEBUG | 调试信息，详细的程序运行信息 | 变量值、方法参数 |
| INFO | 一般信息，重要的业务流程节点 | 用户登录、订单创建 |
| WARN | 警告信息，可能的问题但不影响运行 | 参数缺失使用默认值 |
| ERROR | 错误信息，影响正常运行的错误 | 数据库连接失败 |

## 4. 最佳实践

### ✅ 推荐做法

```java
// 1. 使用占位符而不是字符串拼接
logger.info("用户 {} 购买了商品 {}, 金额: {}", userId, productId, amount);

// 2. 在DEBUG级别前加判断
if (logger.isDebugEnabled()) {
    logger.debug("详细数据: {}", complexObject.toString());
}

// 3. 记录关键业务节点
logger.info("订单 {} 支付成功, 金额: {}", orderId, amount);

// 4. 异常时记录完整堆栈
try {
    // 业务逻辑
} catch (Exception e) {
    logger.error("操作失败", e);  // 第二个参数传异常对象
}
```

### ❌ 避免做法

```java
// 1. 避免字符串拼接
logger.info("用户" + userId + "购买了商品" + productId);  // ❌

// 2. 避免在循环中打印大量日志
for (Item item : items) {
    logger.info("处理商品: {}", item);  // ❌ 数据量大时会很慢
}

// 3. 避免吞掉异常
try {
    // 业务逻辑
} catch (Exception e) {
    logger.error("出错了");  // ❌ 没有记录异常堆栈
}

// 4. 避免记录敏感信息
logger.info("用户密码: {}", password);  // ❌ 绝对不能记录
```

## 5. 自动日志记录

系统已配置AOP切面，会自动记录所有Controller层的请求和响应：

- 请求URI和方法
- 客户端IP地址
- 请求参数
- 响应结果
- 执行时间
- 异常信息

无需手动添加代码，自动记录到 `book-platform-access.log` 和 `book-platform-business.log`。

## 6. 日志文件位置

日志文件存储在项目的 `logs/` 目录下：

```
backend/
├── logs/
│   ├── book-platform-all.log       # 所有日志
│   ├── book-platform-info.log      # INFO级别日志
│   ├── book-platform-error.log     # ERROR级别日志
│   ├── book-platform-business.log  # 业务日志
│   ├── book-platform-access.log    # 访问日志
│   └── history/                    # 历史日志归档
│       ├── book-platform-all-2024-01-15.0.log
│       └── ...
```

## 7. 查看日志

### Linux/Mac
```bash
# 实时查看日志
tail -f logs/book-platform-all.log

# 查看最近100行
tail -n 100 logs/book-platform-error.log

# 搜索特定内容
grep "订单创建" logs/book-platform-business.log
```

### Windows PowerShell
```powershell
# 实时查看日志
Get-Content logs\book-platform-all.log -Wait -Tail 50

# 查看最近100行
Get-Content logs\book-platform-error.log -Tail 100

# 搜索特定内容
Select-String -Path logs\book-platform-business.log -Pattern "订单创建"
```

## 8. 环境配置

### 开发环境（dev）
```yaml
spring:
  profiles:
    active: dev
```
- 输出到控制台（彩色显示）
- 输出到文件
- 日志级别：DEBUG

### 生产环境（prod）
```yaml
spring:
  profiles:
    active: prod
```
- 仅输出到文件
- 日志级别：INFO
- 关闭控制台输出提升性能
