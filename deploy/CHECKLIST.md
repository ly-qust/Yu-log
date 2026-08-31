# 上线检查清单

- [ ] 已修改默认管理员密码，默认 `yu_admin / Yu@123456` 不再可用
- [ ] `JWT_SECRET` 已配置为 32 字符以上随机字符串
- [ ] 生产数据库密码、Redis 密码已更换，不使用示例值
- [ ] `.env` 未提交到 Git
- [ ] 后端使用 `SPRING_PROFILES_ACTIVE=prod`
- [ ] `/api/dev/**` 在生产环境不可访问
- [ ] `FRONTEND_ORIGIN` 与正式域名一致
- [ ] HTTPS 已配置并可自动续期
- [ ] `/api/admin/**` 未登录返回 401
- [ ] 上传目录使用 Docker volume 或宿主持久化目录
- [ ] `client_max_body_size` 不小于后台上传限制
- [ ] 数据库备份脚本已验证，备份文件可恢复
- [ ] MySQL 数据卷和上传目录有明确备份策略
- [ ] 前端生产 `VITE_API_BASE_URL=/api`
- [ ] 生产日志级别不是 SQL DEBUG
- [ ] 服务器防火墙只开放必要端口
- [ ] 已验证登录、发文、上传、评论/留言审核、项目/笔记/时间线展示
