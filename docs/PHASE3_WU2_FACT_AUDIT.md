# 阶段三 WU2 · Draft Fact Audit

审计对象：`docs/drafts/phase3-wu2-personal-blog-cms.md`
审计日期：2026-09-02
结论：通过，可进入人工审核；保持 Draft，不发布。

## 1. 逐项核对

| 草稿主张 | 来源 | 结果 |
| --- | --- | --- |
| Yu-log 是个人博客 CMS | `README.md` | 通过 |
| 项目从 Stitch 静态 UI 演进 | `V2__seed_data.sql` 的项目描述、内容资产索引 | 通过；已避免展开第三方产品细节 |
| 初始模型包含文章、项目、笔记、时间线、评论、留言、站点配置、操作日志 | `V1__init_schema.sql` | 通过 |
| Article/Project/Note/TimelineEvent 的字段职责 | 对应 Entity 文件、V1/V5 migration | 通过 |
| 技术栈为 Java 21、Spring Boot 3、MyBatis-Plus、MySQL 8、Redis、Vue 3、TypeScript、Vite、Vue Router、Pinia、Axios、Tailwind CSS | `README.md`、`backend/pom.xml` | 通过 |
| 公开端和 Admin 端存在清晰路由边界 | `frontend/src/router/index.ts` | 通过 |
| Admin 需要认证与 Admin 权限 | `frontend/src/router/index.ts`、README | 通过 |
| V3–V7 分别补充阅读时间、回复字段、内容元数据/可见性、文章索引、安全字段 | 对应迁移文件名与 SQL | 通过 |
| Admin 有草稿、Markdown 预览、互动审核等能力 | Admin views/components、既有 QA 脚本和报告 | 通过；使用“具备入口/能力”，未称生产验证 |
| 2026-09-02 本地快照为 3 篇文章记录、2 个项目、4 条笔记、5 条成长记录 | 只读本地 API 检查 | 通过；已注明是开发环境快照 |
| 当前数据含阶段验证遗留记录 | 只读本地 API 检查 | 通过；已明确排除，不进入公开内容 |
| “CMS 不只是一个编辑器” | 基于模型和界面的文章判断 | 通过；使用观点语气 |
| 渐进式演进更适合个人项目 | 个人经验判断 | 通过；未写成普遍技术定律 |

## 2. 脱敏复核

- 未出现账号、密码、密码哈希、JWT secret、数据库密码或 Redis 密码。
- 未出现本机绝对路径、真实部署地址、邮箱、IP hash、User-Agent 或操作日志内容。
- 未出现 GitHub 占位地址 `your-name`。
- 未出现 `6B测试`、`adfa` 等阶段验证记录的具体内容。
- 未写入当前封面 404 的具体资源地址。
- 未调用发布 API、未向数据库写入草稿、未上传媒体。

## 3. 不应在人工审核前补写的内容

- 线上访问量、用户数、SLA、吞吐、缓存命中率或性能提升百分比。
- “已经上线”“零缺陷”“生产可用”“通过安全审计”等结论。
- 任何真实账号、部署环境、私有链接和未确认素材版权。

## 4. 人工审核关注点

1. 第一人称语气是否符合 Yu 的真实经历。
2. “可运营”的定义是否符合作者想对外表达的边界。
3. 是否允许公开 Vue/Spring Boot/MySQL/Redis/Flyway/JWT 等技术实现细节。
4. 是否需要补充某个阶段的真实日期、截图或代码片段；若补充，必须再次做事实核对与脱敏。

本次审计完成后，文章停留在本地 Draft，等待人工审核。
