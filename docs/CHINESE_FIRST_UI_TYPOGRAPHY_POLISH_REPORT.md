# Chinese-first UI & Typography Polish Report

日期：2026-09-02
范围：Yu-log 公开前端 + Admin 界面文案收口、中文字体栈、少量受控文字动效。
边界：未修改 API、数据库、JWT、Flyway、文章/项目数据结构、Markdown pipeline 或 Admin 数据逻辑。

## 1. English Density Audit

本轮将用户可见的普通 UI 调整为中文主语言：导航、按钮、标题、筛选、分页、状态、表单、错误、空状态与搜索均以中文为第一信息层。英文不再承担主要解释职责。

保留的英文主要集中在品牌、技术名称、Section Eyebrow、Mono Label、代码、快捷键和签名句。当前视觉目标是中文约占普通 UI 的 75%–90%，英文约占 10%–25%。

## 2. Localization Strategy

采用“中文主标题 + 小型英文辅助”的结构，例如“精选项目 / SELECTED WORK”“成长轨迹 / GROWTH LOG”。没有引入 i18n 框架，也没有把所有内容塞入一个大型翻译文件。

新增 `frontend/src/config/ui-copy.ts`，只集中维护高频共享文案：搜索、复制、返回、分页、主题切换、无结果和命令面板提示。页面自身的内容文案仍留在所属组件，便于结合真实语境维护。

## 3. Navigation

公开导航统一为：首页、文章、项目、笔记、成长、留言、关于。搜索按钮改为“搜索”，移动端抽屉、主题入口和关闭动作均以中文为主；`YU.LOG` 品牌名与 `⌘ K` 快捷键保留。

## 4. Homepage

首页改为中文优先的个人表达：真实 Hero 数据继续来自 About/Home API，`Online & Learning` 映射为“在线学习中”，统计显示为“文章 / 项目 / 笔记”。CTA 调整为“看看我做过的项目”“读点文章”“继续逛逛”。

`BUILD · LEARN · DOCUMENT · GROW` 以及 Closing 的 `Still learning. / Still building. / Still growing.` 保留为 Yu-log 的品牌签名，而不是主要说明文字。技术节点仍显示真实技术名。

## 5. Articles

文章列表、文章详情、目录、复制、上一篇/下一篇、发布时间、更新时间、预计阅读、阅读数、评论与错误/空状态均已中文化。文章 SEO 默认改为“文章｜YU.LOG · Yu 的个人技术博客”，详情保留文章真实标题和技术名。

代码块复制按钮由 `Copy` 改为“复制”，Markdown 渲染器的纯文本和标题兜底也改为中文。

## 6. Projects

项目列表与详情使用“项目”“精选项目”“查看详情”“项目详情”等中文主文案。`Architecture / Workflow / Technology / Challenges / Decisions / Screens` 等说明层改为中文；工程图中的 Vue3、Spring Boot、MySQL、Redis、Tailwind CSS 等技术名称保持原文。

## 7. Notes

笔记主标题使用“随手记”或“正在生长的笔记”，操作统一为“打开笔记”“更新于”“主题”。空状态保持适度个性化：“数字花园正在生长”，没有扩展成大段诗意文案。无主题兜底由 `NOTE` 改为“笔记”。

## 8. Timeline

时间线以“成长轨迹 / GROWTH LOG”“一路留下的节点”为主。记录类型统一显示为：项目、学习、竞赛、证书、经历、里程碑；筛选、分页和空状态均为中文。

## 9. About

About 页面调整为“关于我 / ABOUT”，主标题为“系统背后，是一个仍在学习的人”。教育经历、学习方式、联系方式和技能说明均为中文；技能分组改为“后端工程、数据与缓存、基础设施、前端体验、AI 应用、其他工具”。技术标签继续使用英文技术名。

## 10. Command Palette

命令面板标题改为“搜索这片数字花园”，Placeholder 为“搜索文章、项目和笔记……”。导航、主题、结果分类、快捷动作和无结果提示均中文优先。

保留 `SEARCH THE GARDEN` 作为弱化品牌点缀；`↑ ↓ 选择`、`Enter 打开`、`Esc 关闭` 与 `Ctrl/⌘ K` 保持快速可读。

## 11. Admin

Admin 导航与页面标题统一为：控制台、文章、项目、笔记、时间线、评论、留言、站点配置、账号安全。登录、保存草稿、发布、删除、编辑、预览、本地草稿恢复、离开保护、审核状态与错误提示均已中文化。

Admin 没有加入 Kinetic Typography，也没有改变写入链、权限或数据逻辑；`Markdown`、`JSON`、`GitHub`、`BCrypt` 等技术字段保留原文。

## 12. Kinetic Typography

新增 `frontend/src/components/common/KineticText.vue`。它只在 Hero 中文介绍中使用，按标点切分为短语组，以轻微的 opacity、blur 和 offset 顺序进入；没有采用全站逐字跳动。

Hover 只在 fine pointer 设备上对大型标题产生约 1px 级响应，未使用字符大幅散开或周期性自动跳动。系统仍遵循 “Calm by default. Alive on interaction.”。

## 13. Chinese Font Stack

全局字体栈优先使用 `PingFang SC`、`Microsoft YaHei`、`Noto Sans CJK SC`、系统字体；英文字体仍使用 `Space Grotesk`、`Inter` 等既有品牌字体。未在线加载大型中文字体。

中文页面设置了正常字距与更舒展的行高；中文大标题使用 600–700 级字重，并在移动端通过现有 clamp 尺寸缩放。

## 14. Microcopy

高频文案统一为：正在加载、重新加载、没有找到相关内容、换个关键词试试？、返回、复制、已复制、复制失败、已完成、进行中、草稿、已发布、上一页、下一页、关闭。

空状态采用页面语境文案，例如文章“没有匹配的文章”、项目“项目正在生长”、笔记“数字花园正在生长”，避免统一成生硬的“暂无数据”。

## 15. Accessibility

`KineticText` 的视觉短语设置为 `aria-hidden`，同时提供包含完整句子的 `.sr-only` 文本，屏幕阅读器不会逐字朗读。命令面板继续使用 dialog、listbox/option、焦点管理与 `aria-activedescendant`。

主要按钮、抽屉关闭、图片预览、代码复制和表单控件均保留中文 aria-label 或可访问名称。

## 16. Reduced Motion

`prefers-reduced-motion: reduce` 下，KineticText 会立即显示完整文字，取消 opacity、blur、transform、animation 和 transition；命令面板、Hero 光效及已有页面动效也继续遵循既有 reduced-motion 规则。

## 17. Responsive

针对 375、430、768、1024、1440、1920 宽度验证中文折行、导航、Hero、Section Heading、CTA、筛选器、项目架构、时间线和命令面板。公开路由全部通过横向溢出检查；Admin 页面完成 375–1440 检查，Admin 登录页额外覆盖 1920。

## 18. Before / After

| 区域 | Before | After |
| --- | --- | --- |
| 导航 | Home / Articles / Projects / Notes | 首页 / 文章 / 项目 / 笔记 |
| 首页 CTA | View Project / Read Article | 查看详情 / 读文章 |
| 文章阅读 | Back to Writing / Copy / Previous Article | 返回文章 / 复制 / 上一篇 |
| 时间线 | Growth Archive | 成长轨迹 / GROWTH LOG |
| 关于 | Behind the systems... | 系统背后，是一个仍在学习的人。 |
| 命令面板 | SEARCH THE GARDEN | 搜索这片数字花园 + SEARCH THE GARDEN |
| Admin 编辑器 | Preview / Editor | 预览 / 编辑 |

## 19. QA

- `frontend`: `npm.cmd run build` 通过，`vue-tsc --noEmit` 与 `vite build` 均通过，354 个模块成功转换。
- `scripts/qa-chinese-first-real.mjs`：公开核心路由、404、Admin 登录，覆盖 375/430/768/1024/1440/1920；全部检查通过。
- `scripts/qa-articles-real.mjs`：真实文章列表、搜索、详情、Markdown、封面回退、移动端；全部检查通过。
- `scripts/qa-projects-real.mjs`：真实项目列表、筛选、详情、架构、工作流、主题与多断点；全部检查通过。
- `scripts/qa-content-real.mjs`：About、Timeline、Notes、详情、筛选、SEO 与多断点；全部检查通过。
- `scripts/qa-experience-real.mjs`：Command Palette、主题切换、架构交互、时间线进度、图片预览准备状态、移动端与 Reduced Motion；全部检查通过。
- `scripts/qa-admin-content.mjs`：登录、控制台、文章预览、本地草稿、编辑器复用、站点配置、审核页与 Admin 多断点；全部检查通过。

验收期间 `failedRequests` 均为空。控制台仍记录了既有数据库文章封面路径 `/uploads/2026/04/article-cover-05b0db5807d445bc9f339a5feb627ce4.png` 的 404 诊断；该内容资源由现有数据库数据引用，文章封面已有安全回退，不在本轮数据库/媒体迁移范围内，因此未修改业务数据。

## 20. Remaining English Terms

保留项包括：`YU.LOG`、`Cybernetic Garden`、`BUILD · LEARN · DOCUMENT · GROW`、`Still learning. / Still building. / Still growing.`、`SEARCH THE GARDEN`、Section Eyebrow、Mono Label、Vue3、Spring Boot、Java、MySQL、Redis、RabbitMQ、Docker、Vue、TypeScript、JWT、Flyway、RAG、LLM、API、REST、Outbox / Inbox、CAS、RBAC、Markdown、JSON、GitHub、Demo、HTTP、BCrypt，以及代码和快捷键名称。

## 21. Why They Were Kept

这些英文要么是品牌识别，要么是不可替代的技术术语，要么是辅助层的视觉标签，继续保留可以维持 Yu-log 的国际化开发者气质；它们已被压到中文主标题、中文操作和中文说明之后，不再构成主要理解成本。

结论：Chinese-first Content & Kinetic Typography Polish 已完成。公开前端进入冻结状态，不再创建新的 UI Work Unit；下一步进入阶段三 WU2，先建立事实来源与脱敏 Fact Sheet，再写本地 Draft，完成事实审计后等待人工审核，不自动发布。
