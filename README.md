# Yu Log

Yu Log 是一个个人博客 CMS 系统，前台提供文章、项目、笔记、时间线、关于我和留言板，后台提供内容管理、互动审核、站点配置和文件上传。

## 技术栈

- 前端：Vue 3、TypeScript、Vite、Vue Router、Pinia、Axios、Tailwind CSS
- 后端：Java 21、Spring Boot 3、Spring Security、JWT、MyBatis-Plus、MySQL 8、Redis
- 部署：Docker Compose、Nginx

## 功能模块

- 公开端：首页、文章列表/详情、项目页、笔记页/详情、时间线、关于我、留言板
- 后台：文章、分类、标签、项目、笔记、时间线、评论、留言、站点配置、图片上传
- 运维能力：Redis 缓存、浏览量/点赞异步计数、MySQL 备份脚本、Nginx 反向代理配置

## 目录结构

```text
blog-system/
├── backend/              # Spring Boot 后端
├── frontend/             # Vue 前端
├── deploy/               # Nginx 和部署文档
├── scripts/              # 启动、停止、备份脚本
├── docker-compose.yml    # 完整 Docker 部署
└── docker-compose.local.yml
```

## 本地开发启动

准备 MySQL 和 Redis：

```powershell
docker compose -f docker-compose.local.yml up -d
```

启动后端：

```powershell
cd backend
mvn spring-boot:run
```

启动前端：

```powershell
cd frontend
npm install
npm run dev
```

访问：

- 前端：http://localhost:5173
- 后端健康检查：http://localhost:8080/api/health
- dev 数据库检查：http://localhost:8080/api/dev/db-check

也可以使用：

```powershell
.\scripts\dev-start.ps1
.\scripts\dev-stop.ps1
```

## Docker 一键启动

复制环境变量模板：

```powershell
Copy-Item .env.example .env
```

编辑 `.env`，至少修改数据库密码、Redis 密码和 `JWT_SECRET`。

启动完整系统：

```powershell
docker compose up -d --build
```

访问：

- 前端：http://localhost
- 后端 API：http://localhost/api/health
- 上传资源：http://localhost/uploads/...

停止：

```powershell
docker compose down
```

保留数据卷时不要加 `-v`。如需清空数据再初始化，可执行 `docker compose down -v`。

## 默认管理员

- 用户名：`yu_admin`
- 测试密码：`Yu@123456`

第一次部署后必须修改默认管理员密码。当前后台还没有独立改密页面，可先通过数据库更新 `sys_user.password_hash`，或在后续用户设置功能中处理。

## 环境变量

常用变量见 [.env.example](./.env.example)：

- `MYSQL_ROOT_PASSWORD`、`MYSQL_DATABASE`、`MYSQL_USER`、`MYSQL_PASSWORD`
- `REDIS_PASSWORD`
- `JWT_SECRET`：至少 32 位随机字符串
- `FRONTEND_ORIGIN`：生产域名，例如 `https://example.com`
- `HTTP_PORT`：前端 Nginx 对外端口，默认 `80`
- `STORAGE_MAX_SIZE_MB`

后端生产配置使用：

- `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`
- `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`
- `JWT_SECRET`、`JWT_ACCESS_EXPIRE`、`JWT_REFRESH_EXPIRE`
- `STORAGE_LOCAL_PATH`、`STORAGE_PUBLIC_PREFIX`、`STORAGE_MAX_SIZE_MB`
- `SERVER_PORT`、`FRONTEND_ORIGIN`

## 数据库初始化

Docker Compose 的 MySQL 服务会在数据卷首次创建时执行 `backend/src/main/resources/db/*.sql`。生产后端配置中 `spring.sql.init.mode=never`，不会在每次后端启动时重复执行 SQL。

已有 SQL 文件使用幂等写法，适合开发环境反复启动。长期建议接入 Flyway 管理迁移版本。

## 数据库备份

PowerShell：

```powershell
.\scripts\backup-db.ps1
```

Shell：

```bash
sh scripts/backup-db.sh
```

备份文件输出到 `backups/`，该目录已被 `.gitignore` 排除。

## 常见问题

- `docker compose up` 后没有数据：确认是否第一次启动时 SQL 已执行；如果数据卷已存在，MySQL 不会重新执行 `/docker-entrypoint-initdb.d`。
- 上传图片不可访问：确认 `uploads_data` 数据卷存在，Nginx 已代理 `/uploads/`。
- 生产登录失败：确认 `JWT_SECRET` 与后端运行环境一致，数据库种子用户存在。
- CORS 报错：确认 `FRONTEND_ORIGIN` 与浏览器访问域名完全一致。

## 截图

截图可以后续放到 `docs/screenshots/`，当前仓库暂不包含截图文件。
