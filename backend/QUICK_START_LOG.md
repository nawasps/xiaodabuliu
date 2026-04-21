# 日志模块 - 快速开始

## 🚀 立即使用

### 1. 启动项目

```bash
cd backend
mvn spring-boot:run
```

或者在IDE中运行 `BookApplication.java`

### 2. 测试日志功能

项目已包含测试控制器，访问以下接口测试日志：

#### 测试基本日志
```bash
GET http://localhost:8080/api/test/log/basic
```

#### 测试LogUtils工具类
```bash
GET http://localhost:8080/api/test/log/utils
```

#### 测试异常日志
```bash
GET http://localhost:8080/api/test/log/error
```

#### 测试慢查询日志
```bash
GET http://localhost:8080/api/test/log/slow
```

### 3. 查看日志文件

启动项目后，会在 `backend/logs/` 目录下生成以下日志文件：

```
backend/
└── logs/
    ├── book-platform-all.log       # 所有日志
    ├── book-platform-info.log      # INFO级别
    ├── book-platform-error.log     # ERROR级别
    ├── book-platform-business.log  # 业务日志
    └── book-platform-access.log    # 访问日志
```

### 4. 实时查看日志（PowerShell）

```powershell
# 查看所有日志
Get-Content backend\logs\book-platform-all.log -Wait -Tail 50

# 查看错误日志
Get-Content backend\logs\book-platform-error.log -Wait -Tail 50

# 查看业务日志
Get-Content backend\logs\book-platform-business.log -Wait -Tail 50
```

## 📖 在代码中使用日志

### 方法1: 传统方式

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    
    public void createBook(Book book) {
        logger.info("创建图书: {}", book.getTitle());
        // 业务逻辑...
    }
}
```

### 方法2: 使用LogUtils工具类

```java
import com.campus.book.util.LogUtils;

@RestController
@RequestMapping("/books")
public class BookController {
    private static final Logger logger = LogUtils.getLogger(BookController.class);
    
    @PostMapping
    public Result<Book> create(@RequestBody BookDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 记录业务操作
        LogUtils.logBusiness(logger, userId, "创建", "图书", 
            "书名: " + dto.getTitle());
        
        // 业务逻辑...
        return Result.success(book);
    }
}
```

## ✨ 自动日志功能

无需编写任何代码，系统会自动记录所有Controller的请求和响应：

- ✅ 请求URI和方法
- ✅ 客户端IP
- ✅ 请求参数
- ✅ 响应结果
- ✅ 执行时间
- ✅ 异常信息

这些日志会自动写入 `book-platform-access.log` 和 `book-platform-business.log`

## 🎯 常用场景

### 记录业务操作

```java
LogUtils.logBusiness(logger, userId, "下单", "订单", 
    String.format("订单号: %s, 金额: %.2f", orderId, amount));
```

### 记录异常

```java
try {
    orderService.createOrder(orderDTO);
} catch (Exception e) {
    LogUtils.logError(logger, "创建订单失败", e);
    return Result.error("订单创建失败");
}
```

### 记录性能

```java
long start = System.currentTimeMillis();
// 业务逻辑...
long duration = System.currentTimeMillis() - start;
LogUtils.logPerformance(logger, "queryBooks", duration);
```

## 📝 日志级别选择

| 级别 | 使用场景 | 示例 |
|------|---------|------|
| DEBUG | 调试信息 | 变量值、方法参数 |
| INFO | 重要业务节点 | 用户登录、订单创建 |
| WARN | 警告但不影响运行 | 参数缺失使用默认值 |
| ERROR | 影响正常运行的错误 | 数据库连接失败 |

## ⚙️ 切换环境

### 开发环境（控制台+文件）
```yaml
# application.yml
spring:
  profiles:
    active: dev
```

### 生产环境（仅文件）
```yaml
# application.yml
spring:
  profiles:
    active: prod
```

## 🔍 常见问题

### Q: 日志文件在哪里？
A: `backend/logs/` 目录下

### Q: 如何查看实时日志？
A: 使用 `Get-Content logs\*.log -Wait -Tail 50` (PowerShell)

### Q: 日志文件太大怎么办？
A: 系统会自动滚动归档，单个文件超过100MB会自动切割

### Q: 如何调整日志级别？
A: 修改 `application.yml` 中的 `logging.level` 配置

### Q: 为什么看不到DEBUG日志？
A: 确保当前环境是dev，并且日志级别设置为DEBUG

## 📚 更多文档

- **详细配置**: 查看 `LOG_CONFIG_SUMMARY.md`
- **使用指南**: 查看 `LOG_USAGE.md`
- **目录说明**: 查看 `logs/README.md`
- **配置文件**: 查看 `src/main/resources/logback-spring.xml`

## ✅ 验证清单

- [x] Logback配置文件已创建
- [x] 日志工具类已创建
- [x] AOP日志切面已创建
- [x] 全局异常处理已更新
- [x] Maven依赖已添加
- [x] 测试控制器已创建
- [x] 文档已完善
- [x] 编译通过

## 🎉 完成！

日志模块已成功集成，现在可以：
1. ✅ 启动项目
2. ✅ 访问测试接口
3. ✅ 查看日志文件
4. ✅ 在代码中使用日志

如有问题，请查看详细文档或检查配置文件。
