# 部署说明

## 服务器要求

- Linux 服务器，建议 2C2G 起步
- Docker 24+ 和 Docker Compose v2
- 一个已解析到服务器的域名
- 80/443 端口可访问

## 安装 Docker

Ubuntu 示例：

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
docker version
docker compose version
```

重新登录 shell 后继续。

## 配置环境变量

```bash
cp .env.example .env
vi .env
```

必须修改：

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_PASSWORD`
- `REDIS_PASSWORD`
- `JWT_SECRET`
- `FRONTEND_ORIGIN=https://你的域名`
- `PUBLIC_BASE_URL=https://你的域名`
- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`
- `TRUSTED_PROXY_CIDRS`（通常包含 Docker Nginx 所在私有网段）

生产 Compose 对数据库、Redis、JWT 和管理员 Secret 使用必填校验；缺少变量会拒绝解析或启动。不要把真实 `.env` 提交 Git。

`JWT_SECRET` 建议使用 32 字符以上随机字符串。

## 构建并启动

```bash
docker compose up -d --build
docker compose ps
```

验证：

```bash
curl http://localhost/api/health
curl http://localhost/ 
```

首次启动时后端由 Flyway 执行 V1–V7；MySQL 不再挂载 `/docker-entrypoint-initdb.d`。

已有 V1–V6 数据库的首次升级流程：

1. 完成 `scripts/backup-db.ps1` 或 `scripts/backup-db.sh` 备份，并验证备份可导入临时库。
2. 确认当前 Schema 与仓库 V1–V6 一致。
3. 一次性设置 `YU_LOG_FLYWAY_BASELINE_ON_MIGRATE=true`、`YU_LOG_FLYWAY_BASELINE_VERSION=6`。
4. 启动后端，日志应显示 baseline=6 并执行 V7。
5. 验证 `flyway_schema_history`、健康检查、登录和公开页面，然后把 baseline 开关恢复为 `false`。

## 配置域名

将域名 A 记录解析到服务器公网 IP。确认 `FRONTEND_ORIGIN` 与最终访问协议和域名一致，例如：

```text
FRONTEND_ORIGIN=https://blog.example.com
```

## HTTPS

推荐使用宿主机 Nginx + Certbot：

```bash
sudo apt install nginx certbot python3-certbot-nginx
sudo certbot --nginx -d blog.example.com
```

如果使用本仓库的 `frontend` 容器直接暴露 80，可以在宿主机 Nginx 中反代到容器端口，或改造 `deploy/nginx/nginx.conf` 增加 443 server block。

关键 Nginx 设置：

- `try_files $uri $uri/ /index.html;`
- `/api/` 代理到后端
- `/uploads/` 代理到后端
- `client_max_body_size 5m;`

## 数据库备份

```bash
sh scripts/backup-db.sh
```

备份输出到 `backups/`。建议配置 cron 定时执行，并把备份同步到对象存储或另一台服务器。

恢复演练示例（请使用临时数据库名，不要覆盖生产库）：

```bash
docker compose exec -T mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" \
  -e 'CREATE DATABASE yu_log_restore_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;'
docker compose cp backups/yu_log-<timestamp>.sql mysql:/tmp/yu-log-restore.sql
docker compose exec -T mysql sh -c \
  'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" yu_log_restore_test < /tmp/yu-log-restore.sql'
docker compose exec -T mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" \
  -e 'DROP DATABASE yu_log_restore_test;'
```

## 查看日志

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
docker compose logs -f redis
```

## 更新部署

```bash
git pull
docker compose up -d --build
docker compose ps
curl http://localhost/api/health
```

数据库升级由 Flyway 自动执行；部署前先备份，部署后检查迁移历史和健康状态。

## 回滚

保留上一个可用镜像或 Git tag。出现问题时：

```bash
git checkout <last-good-tag>
docker compose up -d --build
```

如果迁移已经执行且新版本无法启动：停止应用，按备份恢复流程恢复数据库，再回退镜像或 Git tag。不要只回退代码而跳过数据库兼容性检查。
