# Yu-log · 本地内容资产索引

> 阶段三 · 工作单元 1 / Local Content Asset Discovery  
> 扫描日期：2026-09-01  
> 扫描方式：只读文件名、项目结构、README/架构文档、代码职责、SQL/测试和已有截图元信息；不改动源项目、不上传、不自动发布。

## 1. 扫描边界与安全规则

扫描根目录为 `D:\project`，重点覆盖其中可识别的 Java/Spring、Python/FastAPI、Vue/前端、AI、数据分析和小程序项目，以及当前仓库 `D:\project\Yu-log`。

以下目录没有作为内容候选读取：`.git`、`node_modules`、`target`、`dist`、`build`、`uploads`、`backups`、`.idea`、`.vscode`、`.gradle`、`.m2`、`.venv`、`venv`、`__pycache__`、`uni_modules`、临时输出目录和大型依赖目录。以下文件名模式直接跳过：`.env*`、credential、secret、token、password、cookie、SSH key、PEM、P12、PFX 等。

敏感文件处理原则：只记录“敏感文件跳过”，不读取、不复制、不摘录、不进入索引正文。包含本地演示账号、连接配置、私有项目参数或 AI key placeholder 的 README/配置说明统一标记为中高风险，后续写作必须重新脱敏。

## 2. 项目级盘点

| Project | 可审阅候选 | Markdown | Code | SQL | Docs | Images | 初步结论 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| Yu-log | 330 | 7 | 275 | 7 | 0 | 1 | 当前主项目，内容资产最完整，优先级最高 |
| SmartRenew-Platform | 455 | 17 | 394 | 12 | 0 | 0 | 高质量企业架构与并发一致性素材 |
| Enterprise Fixed Assets Full Lifecycle Management System | 175 | 3 | 142 | 17 | 0 | 0 | 业务建模和资产状态流转素材 |
| servers-jiankong | 94 | 4 | 73 | 0 | 0 | 3 | 系统监控、安全边界和实时数据素材 |
| py-data-analysis | 76 | 6 | 58 | 2 | 1 | 0 | 教育数据、AI 辅助教学和文件解析素材 |
| Smart Review Suite | 75 | 1 | 70 | 0 | 0 | 0 | RBAC、审计、文件库和异步审查架构素材 |
| uniapp | 146 | 7 | 77 | 1 | 2 | 25 | 小程序/点餐/云函数素材，私有配置较多 |
| qiuzhao | 26 | 1 | 15 | 0 | 0 | 3 | 校招数据抓取、清洗、分析和可视化素材 |
| langchaintest | 19 | 3 | 11 | 0 | 0 | 0 | AI Code Helper、RAG、Guardrail、MCP 素材 |
| gov-appointment-system | 20 | 0 | 15 | 2 | 0 | 0 | 预约、Redis 和状态一致性素材 |
| gaobingfa-test | 9 | 1 | 6 | 0 | 0 | 0 | 秒杀/库存/消息队列入门素材 |
| servers-jiankong-pro | 0 | 0 | 0 | 0 | 0 | 0 | 当前未发现可审阅候选，暂不纳入 |
| data-analysis | 0 | 0 | 0 | 0 | 0 | 0 | 当前目录未发现可审阅候选，暂不纳入 |

> 统计按允许审阅的文本、源代码、SQL、文档和图片扩展名估算，并已排除上述依赖/敏感路径；不代表所有文件都适合公开发布。

## 3. Asset Index

| Source | Type | Project | Topic | Technologies | Blog Value | Sensitivity Risk | Suggested Output | Priority |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `README.md` + `backend/src/main/resources/db/V1__init_schema.sql`–`V7__add_admin_security_fields.sql` | README / schema / migration | Yu-log | 从静态 UI 到 CMS、内容模型、迁移演进 | Vue 3、Spring Boot 3、MySQL、Redis、Flyway、Nginx | 很高：有完整真实项目上下文 | 中：seed/config 需脱敏，不能复制凭据或真实数据 | 长文《一个个人博客 CMS 如何从 UI 壳层走向可运营系统》 | P0 |
| `front-picture/*.html` + `front-picture/Image 19.markdown` | UI source / design notes | Yu-log | Cybernetic Garden、阅读系统、Project Lab、Timeline、Admin 信息架构 | HTML、CSS、Glassmorphism、响应式布局 | 很高：可做设计复盘和迭代前后对照 | 低：发布前确认图片/素材版权 | 设计复盘《个人博客的 living terminal + digital garden 视觉系统》 | P0 |
| `frontend/src/components/home/*` + `frontend/src/components/layout/*` + `docs/FRONTEND_EXPERIENCE_EXCELLENCE_REPORT.md` | source / QA report | Yu-log | Hero、Command Palette、Motion、主题和可访问交互 | Vue 3、Vue Router、Tailwind CSS、View Transition API | 很高：有实现和真实 QA 证据 | 低：不要公开部署环境凭据 | 系列文章《把静态博客首页做成可探索的数字花园》 | P0 |
| `backend/src/main/resources/db/V5__extend_project_note_timeline_site.sql` + `V6__optimize_article_search_indexes.sql` | SQL migration | Yu-log | 内容扩展、文章搜索、缓存与计数边界 | MySQL、Redis、索引、Flyway | 高：适合解释渐进式 schema evolution | 中：不要原样发布 seed 数据和生产结构细节 | 技术短文《个人博客中的 MySQL、Redis 与计数一致性》 | P1 |
| `backend/src/test/java/com/yu/blog/*` + `scripts/qa-*.mjs` | tests / E2E scripts | Yu-log | 真实写链、公开端验收、安全与回归测试 | JUnit、Playwright、Spring Boot、Vue | 高：可以展示“完成不是截图，而是证据” | 中：测试数据必须保持合成化 | 工程实践《个人项目如何建立从 API 到浏览器的验收链》 | P1 |
| `README.md` + `docs/ARCHITECTURE.md` | README / architecture | SmartRenew-Platform | 活动规则、资格预检、异步审核、额度和发券 | Java 21、Spring Boot、MySQL、Redis、RabbitMQ、Flyway | 很高：复杂度和边界都清晰 | 中高：文档含本地演示配置痕迹，必须脱敏 | 深度文章《Outbox/Inbox、额度预占与补贴券幂等》 | P1 |
| `backend/.../review`、`quota`、`voucher`、`merchant` 包结构 | source topology | SmartRenew-Platform | 消息可靠性、Redis Lua、MySQL 最终裁决、核销幂等 | RabbitMQ、Redis Lua、事务、唯一约束 | 很高：适合拆成并发一致性系列 | 中：业务规则可公开，部署细节需隐藏 | 系列《一个模块化单体如何处理异步审核与资金边界》 | P1 |
| `README.md` + `docs/operations.md` + `docs/deployment.md` | README / ops docs | servers-jiankong | 单机监控、告警、实时推送和安全运行 | FastAPI、Vue 3、psutil、SQLite、WebSocket、Nginx、systemd | 很高：有真实限制、运维和安全边界 | 低中：主机名、网络和部署环境发布前复核 | 案例文章《我如何做一个只读的 Linux 主机监控面板》 | P1 |
| `docs/screenshots/*.png` | screenshots | servers-jiankong | dashboard、实时指标、告警规则 | Vue、ECharts/图表、WebSocket | 高：适合 UI/UX 设计复盘 | 中：确认截图中无主机敏感信息 | 视觉案例《实时监控界面的信息密度与状态设计》 | P1 |
| `README.md` + `analysis/{salary,skill,city}_analysis.py` | README / analysis source | qiuzhao/campus-recruit-analysis | 校招岗位抓取、薪资、技能、城市与学历分析 | FastAPI、SQLite、BeautifulSoup、ECharts、pytest | 很高：数据口径、合规边界和图表都有说明 | 低中：示例数据为脱敏/虚构，不能补充真实个人信息 | 数据实践《从岗位清洗到薪资分布：一个校招分析工具的口径》 | P1 |
| `docs/screenshots/{dashboard,jobs,mobile}.png` | screenshots | qiuzhao/campus-recruit-analysis | 数据看板、岗位筛选、移动端响应式 | HTML、CSS、JavaScript、ECharts | 高：适合做小型数据产品案例 | 低：确认无真实公司/个人敏感数据 | 图表设计复盘《把数据分析做成可读的单页看板》 | P1 |
| `src/main/resources/docs/9-OSI七层模型.md`–`11-数据封装与解封装.md` | study notes | langchaintest/ai-code-helper | 网络基础知识库、AI 辅助学习材料 | Markdown、Spring Boot、RAG | 中高：适合沉淀学习方法和知识库内容 | 低中：system prompt 单独跳过，不公开 | 学习笔记《把网络基础知识整理成 AI 可检索材料》 | P1 |
| `src/main/java/.../rag`、`guardrail`、`mcp`、`tools` | source topology | langchaintest/ai-code-helper | AI Code Helper、RAG、输入护栏、工具调用 | Java、LangChain4j、RAG、MCP、Guardrail | 很高：能形成 AI 工程实践主题 | 中：模型密钥、系统 prompt 和外部服务配置必须跳过 | 技术文章《AI 助手的工具边界、输入护栏和降级策略》 | P1 |
| `README.md` + `docs/系统使用说明.md` + `demo-data/*.md` | README / guide / demo content | py-data-analysis/training-analysis-system | 教学方案、试卷生成、成绩和达成分析 | FastAPI、Vue 3、MySQL、文件解析、AI | 高：业务闭环完整且可解释 | 中高：文档含演示账号/学号痕迹，发布前全部替换 | 案例文章《面向教学数据的 AI 辅助分析系统》 | P2 |
| `README.md` + `docs/PRD.md` | README / PRD | Smart Review Suite | RBAC、审计日志、文件制度库、异步审查 | Spring Boot、Spring Security、MyBatis-Plus、MySQL、Redis、RabbitMQ | 很高：适合解释模块化单体的演进 | 中高：本地演示认证信息只记录风险，不得摘录 | 深度文章《从 JWT/RBAC 到审计与异步任务的单体架构》 | P1 |
| `资产全生命周期功能说明.md` + `API测试文档.md` | feature spec / API doc | Enterprise Fixed Assets Full Lifecycle Management System | 采购、入库、借用、维修、折旧、调拨、报废 | Spring Boot、MyBatis-Plus、Vue、Excel import/export | 高：业务状态机和权限建模很适合案例化 | 中高：部署说明含账号/环境信息，必须脱敏 | 案例文章《企业固定资产为什么需要生命周期状态模型》 | P2 |
| `src/main/java/.../service/*Asset*Service*` + `listener/AssetImportListener.java` | source topology | Enterprise Fixed Assets Full Lifecycle Management System | 资产批量导入与状态流转 | Java、MyBatis-Plus、Excel listener、AOP log | 高：能解释“CRUD 之外的业务联动” | 中：真实单位/账号/数据库内容不得发布 | 技术短文《批量导入、审计日志和资产状态的一致性》 | P2 |
| `src/main/java/.../AppointmentController.java` + `AppointmentServiceImpl.java` + `RedisConfig.java` | source topology | gov-appointment-system | 预约名额、时间段、记录查询和 Redis | Spring Boot、MyBatis-Plus、MySQL、Redis | 中高：适合讲预约并发和幂等入门 | 中：SQL data 与环境配置需脱敏 | 技术短文《预约系统中的名额、时间段与幂等》 | P2 |
| `README.md` + `Stock.java` + `SeckillController.java` + `RabbitMQConfig.java` | README / source | gaobingfa-test | 秒杀库存、消息队列和并发入门 | Spring Boot、JPA、MySQL、RabbitMQ | 中：适合从学习项目讲并发演进 | 中：README/config 含本地演示配置痕迹 | 学习复盘《从库存 CRUD 到真正的秒杀一致性》 | P2 |
| `uniapp/foodpark-user-service`、`uniapp/smart-order-system` 的业务入口与静态图片 | source / UI assets | uniapp | 点餐、用户服务、订单状态、云函数 | Uni-app、Vue、云函数、MySQL/云数据库、uView | 中高：有完整小程序业务场景 | 中高：跳过 private config、云环境参数和账号信息 | 案例文章《从小程序页面到订单云函数的拆分》 | P2 |

## 4. 优先内容批次

### P0：先写 Yu-log 自身

1. 《一个个人博客 CMS 如何从 UI 壳层走向可运营系统》：以真实阶段演进、文章/项目/笔记/时间线数据模型和验收链为主线。
2. 《把静态博客首页做成可探索的数字花园》：使用 Before/After 截图，讲 Hero、Motion、Command Palette、主题和移动端。
3. 《个人项目如何建立从 API 到浏览器的验收链》：展示真实数据库、接口、前端和 Playwright 证据，不展示凭据。

### P1：补充有技术辨识度的外部项目

1. SmartRenew：Outbox/Inbox、Redis Lua 预占、MySQL 最终裁决、发券和核销幂等。
2. servers-jiankong：单机监控、WebSocket 实时指标、告警恢复和非 root 部署。
3. qiuzhao：数据清洗口径、薪资/技能/城市分析、爬虫合规和可视化设计。
4. Smart Review + AI Code Helper：RBAC、审计、异步审查、RAG、Guardrail 和 MCP 工具边界。

## 5. 后续内容生产流水线

```text
Asset Index
  -> 选择 P0/P1 主题
  -> 重新读取原始文件并人工确认事实
  -> 删除凭据、个人信息、主机信息和第三方受限素材
  -> 生成文章草稿 / 项目 Case Study / 学习笔记
  -> 本地 Markdown 渲染与链接检查
  -> 管理员人工审核
  -> 用户明确确认后再发布
```

本轮不执行自动摘要写入数据库、不生成后台文章、不上传媒体、不覆盖或移动原始文件。下一轮如需正式内容创作，应先从 P0 的三篇 Yu-log 主题中选择一篇，确认文章标题、语气和是否公开项目实现细节后再进入草稿阶段。

## 6. 当前结论

- Yu-log 已有足够素材形成“项目实践 + 工程方法 + 学习笔记 + 设计复盘”四条内容线。
- 最有差异化的不是单个 CRUD 项目，而是把前端体验、真实后端、数据迁移、缓存、测试和上线收口串成一条可验证的工程叙事。
- SmartRenew、服务器监控和校招分析适合作为第一批外部项目案例；Smart Review、AI Code Helper、教学分析、资产管理和预约系统作为第二批。
- 任何包含环境变量、演示账号、私有配置、默认密码、个人信息、主机信息或第三方截图版权不明的素材，在人工复核前均不进入公开内容。
