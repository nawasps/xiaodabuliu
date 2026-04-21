# 开发指南

## 环境要求

### 开发环境
- **JDK**: 1.8+
- **Node.js**: 16+
- **npm** / **pnpm**: 最新版
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **IDE**: 
  - 后端：IntelliJ IDEA / Eclipse
  - 前端：VS Code / WebStorm
- **Git**: 2.0+

### 生产环境
- Docker 20.10+
- Nginx 1.20+
- 阿里云ECS或其他云服务器

## 快速开始

### 1. 克隆项目
```bash
git clone <repository_url>
cd <project_name>
```

### 2. 初始化Git子模块（如有）
```bash
git submodule update --init --recursive --depth 1
```

### 3. 启动基础设施（Docker）
```bash
cd docker
docker-compose up -d mysql redis
```

### 4. 初始化数据库
```bash
mysql -h 127.0.0.1 -u root -p < docker/mysql/init.sql
```

### 5. 配置后端
```bash
cd backend
# 修改 src/main/resources/application.yml 中的数据库和Redis配置
```

### 6. 启动后端
```bash
cd backend
mvn spring-boot:run
# 或使用IDE直接运行 BookApplication.java
```

### 7. 启动前端
```bash
cd frontend
npm install
npm run dev
```

### 8. 访问应用
- 前端地址：http://localhost:5173
- 后端API：http://localhost:8080/api

## 项目结构说明

### 前端目录结构
```
frontend/
├── src/
│   ├── api/            # API接口封装
│   │   └── index.js    # 统一导出
│   ├── assets/         # 静态资源（图片、字体等）
│   ├── components/     # 公共组件
│   │   ├── common/     # 通用组件（Pagination、Empty等）
│   │   └── layout/     # 布局组件（Header、Sidebar等）
│   ├── directives/     # 自定义指令（权限、复制等）
│   ├── filters/        # 过滤器（日期格式化、金额格式化等）
│   ├── router/         # 路由配置
│   │   └── index.js    # 路由守卫
│   ├── stores/         # Pinia状态管理
│   │   ├── user.js     # 用户状态
│   │   ├── cart.js     # 购物车状态
│   │   └── message.js  # 消息状态
│   ├── utils/          # 工具函数
│   │   ├── request.js  # Axios封装
│   │   ├── storage.js  # 本地存储
│   │   └── validate.js # 表单验证
│   ├── views/          # 页面组件
│   │   ├── front/      # 用户端页面
│   │   │   ├── home/   # 首页
│   │   │   ├── book/   # 商品相关
│   │   │   ├── cart/   # 购物车
│   │   │   ├── order/  # 订单相关
│   │   │   ├── user/   # 个人中心
│   │   │   ├── message/# 消息中心
│   │   │   └── search/ # 搜索相关
│   │   └── admin/      # 管理端页面
│   │       ├── dashboard/
│   │       ├── user/
│   │       ├── book/
│   │       ├── order/
│   │       ├── review/
│   │       ├── report/
│   │       ├── notice/
│   │       ├── category/
│   │       └── log/
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── public/             # 静态资源（favicon等）
├── vite.config.js      # Vite配置
└── package.json
```

### 后端目录结构
```
backend/
├── src/
│   └── main/
│       ├── java/com/campus/book/
│       │   ├── BookApplication.java    # 启动类
│       │   ├── config/                  # 配置类
│       │   │   ├── SecurityConfig.java  # Spring Security配置
│       │   │   ├── RedisConfig.java    # Redis配置
│       │   │   ├── CorsConfig.java     # 跨域配置
│       │   │   └── WebConfig.java      # Web配置
│       │   ├── controller/             # 控制器
│       │   │   ├── front/              # 用户端API
│       │   │   └── admin/              # 管理端API
│       │   ├── service/                # 业务层接口
│       │   │   ├── impl/               # 业务层实现
│       │   │   └── OrderScheduleService.java # 订单定时任务
│       │   ├── mapper/                 # 数据访问层
│       │   ├── entity/                 # 实体类
│       │   │   ├── User.java
│       │   │   ├── Book.java
│       │   │   ├── Order.java
│       │   │   └── ...
│       │   ├── dto/                    # 数据传输对象
│       │   ├── vo/                     # 视图对象
│       │   ├── common/                 # 公共组件
│       │   │   ├── exception/          # 异常处理
│       │   │   ├── result/             # 统一响应
│       │   │   └── constants/          # 常量定义
│       │   ├── security/              # 安全相关
│       │   │   ├── JWT/               # JWT工具类
│       │   │   └── filter/            # 过滤器
│       │   └── util/                   # 工具类
│       └── resources/
│           ├── mapper/                  # MyBatis映射文件
│           ├── application.yml         # 应用配置
│           └── logback-spring.xml      # 日志配置
└── pom.xml
```

## 开发规范

### 前端规范

#### 命名规范
- **组件名**: PascalCase（如 `UserProfile.vue`）
- **普通文件**: kebab-case（如 `user-profile.vue`）
- **变量/函数**: camelCase
- **常量**: UPPER_SNAKE_CASE
- **CSS类名**: kebab-case

#### 代码风格
- 使用 ESLint + Prettier
- 组件文件不超过500行
- 方法注释使用JSDoc格式
- 优先使用Composition API

#### API调用
- 统一使用 `src/utils/request.js` 封装
- 每个模块对应一个api文件
- 请求方法统一导出

#### 状态管理
- 使用Pinia进行状态管理
- 用户状态、购物车状态使用持久化存储
- 消息状态实时更新

### 后端规范

#### 命名规范
- **类名**: PascalCase
- **方法名**: camelCase
- **变量**: camelCase
- **常量**: UPPER_SNAKE_CASE
- **数据库表**: snake_case
- **字段**: snake_case

#### 分层规范
- **Controller**: 接收请求、参数校验、调用Service
- **Service**: 业务逻辑处理、事务管理
- **Mapper**: 数据访问、SQL编写
- **Entity**: 数据库表映射
- **DTO**: 数据传输对象
- **VO**: 视图对象

#### 接口规范
- RESTful风格URL
- 使用REST注解
- 统一响应格式
- 必要时的参数校验

#### 安全规范
- 密码加密存储（BCrypt）
- SQL参数化查询
- XSS过滤
- 接口防刷
- 敏感数据脱敏

## 常见任务

### 创建新页面

1. 在 `views` 下创建页面组件
2. 在 `router/index.js` 添加路由
3. 如需权限控制，在路由守卫中添加判断

### 添加API接口

1. 后端：在对应Controller添加接口
2. 前端：在 `api/` 下添加接口封装
3. 在模块index中统一导出

### 新增实体

1. 创建Entity类
2. 创建Mapper接口和XML
3. 创建Service接口和实现
4. 创建Controller
5. 如需DTO/VO，一并创建

### 添加定时任务

1. 在 `service/OrderScheduleService.java` 添加方法
2. 配置Quartz cron表达式

## 构建与发布

### 前端构建
```bash
cd frontend
npm run build
# 产物在 dist/ 目录
```

### 后端构建
```bash
cd backend
mvn clean package -DskipTests
# 产物在 target/ 目录
```

### Docker部署
```bash
cd docker
docker-compose up -d
```

### Nginx配置
参考 `docker/nginx/nginx.conf`

## 测试

### 前端测试
```bash
cd frontend
npm run test
```

### 后端测试
```bash
cd backend
mvn test
```

## 注意事项

1. **敏感信息**: 不要提交包含真实密码、密钥等敏感信息的代码
2. **环境分离**: 开发、测试、生产环境配置分开管理
3. **分支管理**: 使用git flow或类似的工作流
4. **代码审查**: 提交前进行自检，重要功能需要代码审查
5. **文档更新**: 新增功能时及时更新文档
