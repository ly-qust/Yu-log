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

首次启动时 MySQL 会执行 `backend/src/main/resources/db` 下的 SQL。生产后端不会自动重复初始化数据库。

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
- `client_max_body_size 10m;`

## 数据库备份

```bash
sh scripts/backup-db.sh
```

备份输出到 `backups/`。建议配置 cron 定时执行，并把备份同步到对象存储或另一台服务器。

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

如更新包含数据库脚本，已存在的 MySQL 数据卷不会自动重新执行 `/docker-entrypoint-initdb.d`。当前阶段请手动执行新增 SQL；后续建议接入 Flyway。

## 回滚

保留上一个可用镜像或 Git tag。出现问题时：

```bash
git checkout <last-good-tag>
docker compose up -d --build
```
