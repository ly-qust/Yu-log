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

编辑 `.env`，必须配置数据库密码、Redis 密码、`JWT_SECRET`、生产管理员账号密码和正式站点地址。

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

## 管理员初始化

生产环境不会从 Git 中初始化固定管理员密码。首次启动时，通过 `ADMIN_USERNAME` 和 `ADMIN_PASSWORD` 创建管理员；两个变量必须来自未提交的部署环境或 Secret 管理器，密码长度需要为 12–72 位。

已有数据库升级后，管理员会被标记为需要改密。登录后进入 Admin 的“账号安全”页面完成修改；修改密码需要当前密码，服务端使用 BCrypt 保存。当前 JWT/Refresh Token 不会因为改密立即撤销，仍按原过期时间有效。

## 环境变量

常用变量见 [.env.example](./.env.example)：

- `MYSQL_ROOT_PASSWORD`、`MYSQL_DATABASE`、`MYSQL_USER`、`MYSQL_PASSWORD`
- `REDIS_PASSWORD`
- `JWT_SECRET`：至少 32 位随机字符串
- `FRONTEND_ORIGIN`：生产域名，例如 `https://example.com`
- `PUBLIC_BASE_URL`：站点公开根地址，用于 Sitemap
- `ADMIN_USERNAME`、`ADMIN_PASSWORD`：首次部署管理员初始化凭证
- `TRUSTED_PROXY_CIDRS`：可信反向代理网段
- `YU_LOG_FLYWAY_BASELINE_ON_MIGRATE`：已有库首次接入 Flyway 时临时设为 `true`
- `YU_LOG_FLYWAY_BASELINE_VERSION`：已有当前 V1–V6 数据库固定为 `6`
- `HTTP_PORT`：前端 Nginx 对外端口，默认 `80`
- `STORAGE_MAX_SIZE_MB`

后端生产配置使用：

- `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`
- `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`
- `JWT_SECRET`、`JWT_ACCESS_EXPIRE`、`JWT_REFRESH_EXPIRE`
- `STORAGE_LOCAL_PATH`、`STORAGE_PUBLIC_PREFIX`、`STORAGE_MAX_SIZE_MB`
- `SERVER_PORT`、`FRONTEND_ORIGIN`

## 数据库迁移

数据库结构由 Flyway 管理，迁移文件位于 `backend/src/main/resources/db/`，当前版本为 V7。生产环境禁用 Spring SQL 初始化和 MySQL entrypoint 自动导入。

新数据库直接启动后端即可由 Flyway 执行 V1–V7。已有 V1–V6 数据库首次升级前必须完成备份，并临时设置 `YU_LOG_FLYWAY_BASELINE_ON_MIGRATE=true`、`YU_LOG_FLYWAY_BASELINE_VERSION=6`；启动成功后恢复为 `false`。该 baseline 仅适用于已核对为当前 Schema 的数据库，不能替代备份。

## 数据库备份与恢复

PowerShell：

```powershell
.\scripts\backup-db.ps1
```

Shell：

```bash
sh scripts/backup-db.sh
```

备份文件输出到 `backups/`，该目录已被 `.gitignore` 排除。脚本不会使用危险默认密码；必须通过 `.env` 或进程环境提供 `MYSQL_PASSWORD`。

恢复前先停止应用，把备份导入临时数据库并核对表数量、核心数据数量和关键 ID；确认无误后再切换数据库。恢复演练命令示例见 [deploy/DEPLOYMENT.md](deploy/DEPLOYMENT.md)。

媒体完整性审计：

```powershell
$env:DB_PASSWORD = '从 Secret 管理器读取'
.\scripts\audit-data.ps1
.\scripts\audit-media.ps1
```

审计脚本只报告孤儿关联、失效封面和未引用文件，不会自动删除。

## 常见问题

- `docker compose up` 后没有数据：确认是否第一次启动时 SQL 已执行；如果数据卷已存在，MySQL 不会重新执行 `/docker-entrypoint-initdb.d`。
- 上传图片不可访问：确认 `uploads_data` 数据卷存在，Nginx 已代理 `/uploads/`。
- 生产登录失败：确认 `JWT_SECRET` 与后端运行环境一致，且 `ADMIN_USERNAME` / `ADMIN_PASSWORD` 已在部署环境中配置。
- CORS 报错：确认 `FRONTEND_ORIGIN` 与浏览器访问域名完全一致。

## 截图

截图可以后续放到 `docs/screenshots/`，当前仓库暂不包含截图文件。
