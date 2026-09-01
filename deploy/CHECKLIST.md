# Yu-log v1.0 上线检查清单

## 发布前

- [ ] 生产 Compose 已通过 `docker compose config`。
- [ ] `MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD`、`REDIS_PASSWORD`、`JWT_SECRET`、`ADMIN_PASSWORD` 来自未提交的 Secret。
- [ ] `ADMIN_USERNAME` 已设置，未使用仓库中的固定凭证。
- [ ] `FRONTEND_ORIGIN` 与 `PUBLIC_BASE_URL` 使用正式 HTTPS 地址。
- [ ] `TRUSTED_PROXY_CIDRS` 只包含实际反向代理网段。
- [ ] 已完成数据库完整备份并验证可恢复。
- [ ] 已记录 Flyway 迁移历史和 Schema 快照。
- [ ] 已确定上传目录和 MySQL 数据卷的备份策略。

## 服务与安全

- [ ] 后端使用 `SPRING_PROFILES_ACTIVE=prod`。
- [ ] 生产环境没有 Spring SQL 初始化和 MySQL entrypoint 初始化挂载。
- [ ] Flyway baseline 仅在已有库首次接入时临时开启，随后恢复关闭。
- [ ] `/api/dev/**` 不存在于生产 profile。
- [ ] `/api/admin/**` 未登录返回 401，普通用户无管理员权限。
- [ ] 登录限流返回真实 HTTP 429。
- [ ] CORS 不是通配符，且只允许正式前端 Origin。
- [ ] Nginx 正确设置 Host、Real-IP、Forwarded-For、Forwarded-Proto。
- [ ] 已启用安全响应头和 CSP。
- [ ] HTTPS、证书自动续期和 HSTS 已在部署环境完成。
- [ ] 默认改密标记已处理，管理员已完成密码更新。

## 数据、媒体与运维

- [ ] 数据完整性审计无孤儿关联。
- [ ] 媒体审计已人工确认失效引用和未引用文件。
- [ ] 上传大小限制在 Spring、业务层和 Nginx 一致。
- [ ] 上传目录使用持久化卷，且不具备脚本执行能力。
- [ ] `/api/health` 和 `/actuator/health` 状态正常且不泄露内部详情。
- [ ] 日志未记录密码、JWT、Refresh Token、完整 Authorization 或文章正文。
- [ ] Docker stdout/宿主机日志轮转策略已配置。
- [ ] 备份有频率、保留周期、异地副本和恢复负责人。

## Smoke QA

- [ ] Home、Articles、Article Detail、Projects、Project Detail、Notes、About、Timeline、Messages 返回正常。
- [ ] 登录、管理员鉴权、改密入口正常。
- [ ] Admin 草稿、发布、上传、评论/留言审核正常。
- [ ] `robots.txt` 和 `sitemap.xml` 正常。
- [ ] 404 页面正常。
- [ ] 375/430/768/1024/1440/1920 宽度回归通过。
- [ ] 前端 `vue-tsc --noEmit` 和 production build 通过。
- [ ] 后端 `mvn test`、空库迁移和已有库升级通过。
- [ ] 真实 Docker Compose 启动、健康检查和重启恢复通过。
