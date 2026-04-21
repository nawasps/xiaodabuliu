# 用户指令记忆

本文件记录了用户的指令、偏好和教导，用于在未来的交互中提供参考。

## 格式

### 用户指令条目
用户指令条目应遵循以下格式：

[用户指令摘要]
- Date: [YYYY-MM-DD]
- Context: [提及的场景或时间]
- Instructions:
  - [用户教导或指示的内容，逐行描述]

### 项目知识条目
Agent 在任务执行过程中发现的条目应遵循以下格式：

[项目知识摘要]
- Date: [YYYY-MM-DD]
- Context: Agent 在执行 [具体任务描述] 时发现
- Category: [代码结构|代码模式|代码生成|构建方法|测试方法|依赖关系|环境配置]
- Instructions:
  - [具体的知识点，逐行描述]

## 去重策略
- 添加新条目前，检查是否存在相似或相同的指令
- 若发现重复，跳过新条目或与已有条目合并
- 合并时，更新上下文或日期信息
- 这有助于避免冗余条目，保持记忆文件整洁

## 条目

[项目采用前后端分离并通过反向代理联通]
- Date: 2026-04-21
- Context: Agent 在执行“扫描仓库结构与配置”时发现
- Category: 依赖关系
- Instructions:
  - 前端为 Vite + Vue3，统一以 `/api` 作为请求前缀
  - 开发环境通过 `vite.config.js` 将 `/api` 代理到 `http://localhost:8080`
  - Docker/Nginx 环境通过 `location /api/` 转发到 `backend:8080/api/`

[后端采用 Spring Boot 单体分层架构]
- Date: 2026-04-21
- Context: Agent 在执行“扫描仓库结构与配置”时发现
- Category: 代码结构
- Instructions:
  - 核心目录结构为 `controller -> service -> mapper -> entity`
  - 同时包含 `dto`、`vo`、`common/result`、`common/exception` 等接口层与通用层封装
  - `Result<T>` 为统一响应结构，返回字段为 `code/message/data`

[本项目构建与运行方式]
- Date: 2026-04-21
- Context: Agent 在执行“扫描仓库结构与配置”时发现
- Category: 构建方法
- Instructions:
  - 前端常用命令为 `npm run dev`、`npm run build`
  - 后端使用 Maven 构建，入口为 `BookApplication`，默认端口 8080、context-path 为 `/api`
  - 一体化容器编排定义在 `docker/docker-compose.yml`（mysql/redis/backend/frontend）

[书籍发布审核流程]
- Date: 2026-04-21
- Context: Agent 在执行“实现书籍审核功能”时发现
- Category: 代码模式
- Instructions:
  - 书籍发布后默认状态应为 `PENDING`，仅管理员审核通过后才能对其他用户可见
  - 普通用户仅可将自己的书籍变更为 `OFFLINE`，不能自行改为 `ON_SALE`
  - 管理端通过 `/admin/book/{id}/audit` 执行通过或驳回

[分支与交付偏好]
- Date: 2026-04-21
- Context: 用户要求便于本地拉取测试
- Instructions:
  - 后续优先直接在 `master` 上操作，或在完成后自动合并回 `master`
  - 本次改动也需要合并到 `master`，保证可直接拉取测试
