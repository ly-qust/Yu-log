# 阶段三 WU2 · First Production Article Fact Sheet

状态：内部写作资料，不是发布内容。
目标文章：《一个个人博客 CMS 如何从 UI 壳层走向可运营系统》
建立日期：2026-09-02

## 1. 写作边界

这篇文章只讨论 Yu-log 自身从静态 UI、工程骨架到内容管理能力的演进。文章中的“可运营”指能够持续维护内容、管理公开状态、处理互动和维护站点配置，不等同于已经完成生产部署、商业化运营或经过独立安全审计。

不写入：账号、密码、密码哈希、JWT secret、环境变量值、真实服务器地址、个人隐私、第三方受限素材、开发机绝对路径、测试数据名称，以及未经来源支持的性能或安全结论。

## 2. 重新读取的事实来源

| 来源 | 用途 | 可使用范围 |
| --- | --- | --- |
| `README.md` | 项目定位、技术栈、功能边界、迁移与部署原则 | 可描述技术栈和模块，不复制环境变量值 |
| `backend/src/main/resources/db/V1__init_schema.sql` | 初始数据模型 | 可描述实体关系和字段职责，不公开用户/互动样本 |
| `backend/src/main/resources/db/V2__seed_data.sql` | 初始内容语境、文章/项目/笔记/时间线主题 | 仅用于事实核对；不照搬 seed、占位链接和测试数据 |
| `backend/src/main/resources/db/V3__add_article_reading_time.sql` | 文章阅读时间字段迁移 | 可描述渐进式 schema 变更 |
| `backend/src/main/resources/db/V4__add_comment_message_reply_fields.sql` | 评论/留言回复能力迁移 | 可描述互动模型扩展 |
| `backend/src/main/resources/db/V5__extend_project_note_timeline_site.sql` | 项目、笔记、时间线、站点配置扩展 | 可描述内容面扩大和可见性/元数据字段 |
| `backend/src/main/resources/db/V6__optimize_article_search_indexes.sql` | 文章搜索相关索引迁移 | 可描述为搜索查询做索引优化，不宣称具体性能收益 |
| `backend/src/main/resources/db/V7__add_admin_security_fields.sql` | Admin 安全字段迁移 | 可描述“存在改密状态字段”，不披露认证细节或凭据 |
| `backend/src/main/java/com/yu/blog/module/*` | Controller、Service、Entity、Mapper 职责 | 可描述代码结构和接口能力 |
| `frontend/src/router/index.ts` | 公开端和 Admin 路由 | 可描述页面范围与受保护 Admin 路由 |
| `frontend/src/components/*`、`frontend/src/views/*` | Vue 页面和交互实现 | 可描述阅读、筛选、预览、草稿等前端能力 |
| `docs/CONTENT_ASSET_INDEX.md` | 内容资产盘点和敏感信息规则 | 作为内容选择与脱敏依据 |
| `docs/FRONTEND_EXPERIENCE_EXCELLENCE_REPORT.md` | 既有前端体验、交互和 QA 记录 | 可描述已完成的体验层，不扩大为生产指标 |
| 2026-09-02 本地只读 API 检查 | 当前数据库中的可访问内容规模 | 只能说明当前本地运行时快照，不能当成长期业务数据 |

## 3. 已验证事实

### 3.1 项目与技术栈

- README 将 Yu-log 定义为个人博客 CMS 系统。
- 前端使用 Vue 3、TypeScript、Vite、Vue Router、Pinia、Axios、Tailwind CSS。
- 后端使用 Java 21、Spring Boot 3、Spring Security、JWT、MyBatis-Plus、MySQL 8、Redis。
- 部署目录包含 Docker Compose 与 Nginx 配置；本文只描述项目具备这些部署配置，不声称当前线上状态。
- seed 中的项目描述明确记录了“从 Stitch 静态 UI 演进为可运营 CMS”的主题，这可以作为文章主线，但 Stitch 本身不是本文要展开的第三方产品评测。

### 3.2 数据模型

V1 初始 schema 包含 `sys_user`、`site_setting`、`category`、`tag`、`article`、`article_tag`、`project`、`note`、`timeline_event`、`comment`、`message`、`operation_log` 等表。

可以据此准确表达：系统的内容单元不只有文章；项目、笔记、成长记录、评论、留言、站点配置和操作日志共同构成了 CMS 的可维护边界。

实体代码进一步确认：Article 有 Markdown 内容、状态、置顶、阅读/点赞/评论计数和阅读时间；Project 有详情、技术栈、可见性与排序；Note 有主题、标签和公开状态；TimelineEvent 有类型、日期、关联文章/项目和可见性。

### 3.3 数据库迁移演进

- V1 建立基础表结构、索引和文章全文搜索字段。
- V3 为文章加入阅读时间字段。
- V4 扩展评论和留言的回复字段。
- V5 为项目增加详情/可见性，为笔记增加主题、标签和公开字段，为时间线增加标签/可见性，并扩展站点配置的分组和说明。
- V6 为文章列表常用条件与标题/摘要增加索引检查和创建逻辑。
- V7 为管理员增加“需要改密”的安全字段，并将现有 Admin 标记为需要改密。

这些是迁移文件直接支持的事实。文章可以将其解释为“边使用边补齐模型”，但不能进一步声称每次迁移都经过完整线上演练，除非另有证据。

### 3.4 后端接口与前端页面

后端存在公开的 Home、About、Articles、Projects、Notes、Timeline、Messages、Comments 接口，以及受保护的 Admin Dashboard、文章、分类、标签、项目、笔记、时间线、评论、留言、站点配置、文件上传和账号接口。

前端 Router 对应的公开路由包括 `/`、`/articles`、文章详情、`/projects`、项目详情、`/notes`、笔记详情、`/timeline`、`/about`、`/messages`；`/admin` 及其子路由要求认证和 Admin 权限。

### 3.5 当前本地运行时快照

2026-09-02 在真实 Spring Boot + MySQL + Redis + Vue/Vite 本地环境进行只读检查：

| Endpoint | 当前结果 |
| --- | --- |
| `/api/home/overview` | Hero、统计、当前学习内容正常返回；统计为 3 条文章记录、2 个项目、4 条笔记、1 条留言 |
| `/api/articles?size=100` | 返回 3 条可访问文章记录 |
| `/api/projects?size=100` | 返回 2 个可访问项目；当前状态快照为 `PLANNING`、`COMPLETED` |
| `/api/notes?size=100` | 返回 4 条可访问笔记 |
| `/api/timeline?size=100` | 返回 5 条可访问成长记录 |

该快照包含开发/阶段验证遗留记录，尤其是标题带有“6B测试”或 `adfa` 的记录；它们只用于识别清理候选，不能进入文章示例，也不能被描述为正式内容规模。

### 3.6 已存在的体验与验收事实

既有报告和本轮 QA 共同确认：公开端具备文章阅读、项目详情、笔记详情、时间线、About、留言板、主题切换、Command Palette、键盘交互、Reduced Motion 与响应式页面；Admin 具备内容表单、Markdown 预览、本地草稿恢复、发布状态和互动审核入口。

这些结论只用于描述当前仓库能力；不延伸为“已上线”“零缺陷”“高并发”或“通过第三方审计”。

## 4. 事实、判断与未知

| 内容 | 分类 | 写作处理 |
| --- | --- | --- |
| 项目从 Stitch 静态 UI 走向 CMS | 来源事实 | 可以作为文章主线 |
| 内容模型从文章扩展到项目、笔记、时间线和互动 | 来源事实 | 结合表结构和前端路由解释 |
| 迁移文件逐步补充字段和索引 | 来源事实 | 可写成演进过程 |
| “CMS 不只是一个编辑器” | 基于事实的判断 | 明确是文章观点，不伪装成测试结论 |
| 渐进式迁移比一次性设计更贴近个人项目 | 经验判断 | 使用“对我来说”“这次实践让我看到”限定 |
| 真实生产吞吐、访问量、SLA、部署规模 | 未知 | 不写 |
| 完整安全审计、灾备恢复成功率、线上可用性 | 未知 | 不写 |
| 技术选型一定优于其他方案 | 未知/主观比较 | 不写绝对结论 |

## 5. 脱敏清单

- 不写管理员用户名、密码、密码哈希、JWT secret、数据库/Redis 密码、环境变量值。
- 不复制 `docker-compose.local.yml` 中的本地默认凭据，也不写入任何本机绝对路径。
- 不使用 seed 中的 GitHub 占位地址 `your-name` 作为真实项目链接。
- 不引用当前数据库中的 `6B测试笔记`、`6B测试时间线`、`adfa` 等阶段验证数据。
- 不暴露真实评论、留言、邮箱、IP hash、User-Agent 或操作日志内容。
- 不把当前本地的封面 404 诊断写成产品故障故事；本轮文章不涉及媒体迁移。
- 不自动写回文章表、不调用发布接口、不上传图片。

## 6. 文章可用事实句

以下句子可直接作为草稿事实基础：

1. “Yu-log 不是先有一套完整 CMS 设计，再去补页面；它是从一组静态 UI 开始，逐步接上真实后端和内容模型。”
2. “当前代码把文章、项目、笔记、时间线、评论、留言、站点配置和操作日志放进同一套可维护边界。”
3. “Flyway 迁移文件记录了阅读时间、回复字段、内容元数据、可见性和搜索索引的逐步补齐。”
4. “公开端通过 Vue Router 暴露阅读和浏览入口，Admin 路由由认证与 Admin 权限保护。”
5. “当前本地运行时可以从真实 API 读到文章、项目、笔记和成长记录；这些数量只是开发环境快照。”

## 7. 草稿验收标准

- 标题必须使用：《一个个人博客 CMS 如何从 UI 壳层走向可运营系统》。
- 使用第一人称经验叙述，但不虚构没有来源支持的动机、时间、团队或用户反馈。
- 每一个架构结论都能回指到 README、迁移、Entity、Controller、Router 或 QA 记录。
- 把“当前仓库有能力”与“已经在线生产验证”严格分开。
- 写完后进行本地 Markdown 渲染检查和事实审计。
- 审计通过后停在 Draft，等待人工审核。
