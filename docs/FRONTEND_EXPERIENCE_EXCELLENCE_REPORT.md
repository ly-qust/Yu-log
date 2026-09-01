# Yu-log · Frontend Experience Excellence Report

> 阶段二 · 工作单元 8 / Frontend Experience Excellence  
> 验收日期：2026-09-01  
> 环境：真实 Spring Boot API `localhost:8080` + Vue/Vite `localhost:5173` + MySQL 3308

## 1. Before 基线评分

本轮 Before 分数是基于真实数据页面、1440px 桌面端和 375px 移动端截图的设计审计分，不是自动化 Lighthouse 分数。

| 维度 | Before | 主要依据 |
| --- | ---: | --- |
| 视觉质量 | 86 | 品牌识别强，但部分页面缺少状态层次 |
| 品牌一致性 | 89 | Cybernetic Garden 语言已成立 |
| 内容清晰度 | 87 | 结构清楚，交互反馈偏少 |
| 交互完成度 | 58 | 页面、卡片和架构图以静态呈现为主 |
| Motion 质量 | 42 | 有基础 reveal，缺少页面级与状态级过渡 |
| Accessibility | 76 | 主要路径可读，交互节点的语义与焦点反馈仍可加强 |
| Responsive | 86 | 关键断点稳定，移动端缺少快捷搜索 |
| Performance | 82 | 无大依赖，但尚未建立统一 motion 预算 |
| 综合体验 | **76** | 强视觉基础，体验层仍有明显可提升空间 |

## 2. Before 主要问题

- 路由切换没有稳定的页面级过渡，页面像“重新加载”而不是连续的阅读空间。
- Navbar 没有全局搜索入口，内容发现依赖导航层级。
- Hero 的数字花园图形是视觉展示，节点没有可交互反馈，也没有指针状态预算。
- Project Architecture、Workflow、Timeline 的真实关系没有在 hover/focus/scroll 时显性化。
- 文章图片没有统一的预览入口；按钮、链接和卡片反馈不完全一致。
- Reduced Motion 只覆盖部分动画，缺少全局审计记录。

## 3. 本轮签名交互

本轮只加入与内容语义有关的轻量交互：

1. `Ctrl/Cmd + K` 或 `/` 打开全局 Command Palette。
2. Hero 节点 hover/focus 高亮对应 signal line，并以 rAF 节流指针光和轻微 tilt。
3. Project Architecture 与 Workflow 通过真实节点 id 双向联动。
4. Timeline 以滚动位置驱动进度线，并使用 IntersectionObserver 激活当前记录。
5. Markdown 图片支持点击、Enter、Space 打开可访问的预览层。

没有加入声音、粒子背景、custom cursor、WebGL、重型动效库或虚构的数据关系。

## 4. Motion System

在 Tailwind 和全局 CSS 中统一了三档时长：`140ms / 220ms / 420ms`，以及 `standard / emphasized / spring` 三种 easing。按钮、卡片、节点、导航指示线和图片预览都复用这些 token，避免每个页面单独定义不可控的动画。

## 5. 页面过渡

`App.vue` 使用 `Transition mode="out-in"` 处理普通页面切换；Router 在支持 `document.startViewTransition()` 的浏览器中包裹 `push/replace`，不支持时自动回退。Reduced Motion 下跳过 View Transition 和 page transition。

## 6. Theme Transition

主题切换保留 `dark / light / system` 语义，在触发按钮位置记录 `--theme-origin-x/y`，支持 View Transition 时从触发点展开主题揭示；不支持时同步切换，不阻塞用户操作。主题偏好仍写入既有 `yu-log-theme` localStorage key。

## 7. Hero Interaction

Hero 的四个学习节点改为真实 button，补充 `aria-pressed`、focus-visible 和对应 signal line 状态。鼠标只更新一个 requestAnimationFrame，最大 tilt 约 1.8°，离开容器时恢复中心状态；触摸设备隐藏指针光，Reduced Motion 关闭 tilt、pulse、orbit 和 signal animation。

## 8. Command Palette

`CommandPalette.vue` 使用已有 `fetchArticles / fetchNotes / fetchProjects` 接口，输入后 debounce 150ms 并按 Articles、Notes、Projects 分组显示真实结果，同时提供 Navigate 和 Theme 命令。支持：

- Ctrl/Cmd+K、`/`、Escape、上下方向键、Enter、Tab focus trap；
- `role="dialog"`、`role="listbox"`、`role="option"`、active descendant；
- 背景滚动锁定、移动端底部面板、Navbar 桌面与移动入口；
- 真实关键词 `redis` 可打开文章 `1001`。

## 9. Project Architecture

架构图节点变为可聚焦 button。Vue3、Spring Boot、MySQL、Redis 四个真实节点共享 active state，连接线随节点变化；Workflow 列表行使用同一组 `nodeId`，hover、focus 和移动端 click 都能反馈到架构图，并通过 `aria-live` 显示当前 Signal focus。

## 10. Timeline

Timeline 主线增加滚动进度层，进度由 viewport marker 与真实 track 几何位置计算，并通过 requestAnimationFrame 合并滚动事件。每条真实记录使用 IntersectionObserver 激活卡片和节点；没有 IntersectionObserver 时回退为静态可见状态，移动端仍使用单列主线。

## 11. Article Reading

MarkdownRenderer 对真实渲染后的图片补充 keyboard semantics 和 preview metadata。图片支持点击及 Enter/Space，预览层使用 Teleport、dialog semantics、Escape 关闭、body overflow lock，并保留 alt/caption。当前真实文章 `1002` 只有 cover image、没有正文图片，因此自动化验收记录为“功能就绪，当前 fixture 无正文图片可打开”。

## 12. Tech Landscape / Notes

Tech Landscape 的每个技术项改为可聚焦 button，选择后显示 `Working signal`，但不虚构“技术属于哪个项目”的关系。Garden Notes 保留 RouterLink 语义，增加真实标题 aria-label、focus-visible 和 reduced-motion 状态。

## 13. Mobile Experience

375px 下保留移动 Navbar、搜索、主题和抽屉入口；Command Palette 变成底部面板。Hero signal nodes、Project Architecture、Timeline 主线均保留可用性，不依赖 hover。真实回归覆盖 375、430、768、1024、1440、1920px，页面没有水平溢出。

## 14. Reduced Motion

覆盖范围包括：首页 reveal、Hero pointer light/tilt、signal/orbit/pulse、按钮 lift、卡片 lift、导航 indicator、Command Palette、图片预览、Timeline 进度和 Project Architecture 反馈。自动化 Reduced Motion context 已验证 Hero light 为 `display:none`、首页 reveal 直接可见。

## 15. Accessibility

本轮补强了 button/link 语义、`aria-label`、`aria-pressed`、focus-visible、dialog/listbox/option、Escape 关闭、keyboard image preview 和 live region。未引入纯 hover 才能访问的隐藏内容；真实 QA 无页面错误或请求失败。

## 16. Performance

没有新增运行时依赖；所有指针/滚动监听使用 rAF 或 passive listener，并在组件卸载时清理。CSS transform 不触发布局重排，图片预览只在打开时创建 overlay。当前报告不宣称 Lighthouse 分数，Performance 结论以构建体积、浏览器交互和无 failed request 为依据。

## 17. Bundle / Build

生产构建通过：350 modules transformed。核心构建输出约为：

- `index.js`：241.00 kB，gzip 86.02 kB；
- `MarkdownRenderer`：229.59 kB，gzip 86.86 kB；
- `index.css`：86.18 kB，gzip 15.25 kB；
- 本轮没有增加任何 npm 依赖。

MarkdownRenderer 体积主要来自既有 markdown/highlight pipeline，本轮仅增加轻量 preview 行为。

## 18. Before / After 截图

截图使用真实 `localhost` 前后端环境生成，共 Before 24 张、After 26 张（基础桌面/移动暗亮主题 + 架构 hover + Timeline progress + Command Palette）。

- [Before 首页暗色](/C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31/experience-before/homepage-dark-1440.png)
- [After 首页暗色](/C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31/experience-after/homepage-dark-1440.png)
- [After Command Palette](/C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31/experience-after-command-palette-dark-1440.png)
- [After 架构 hover](/C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31/experience-after/project-detail-architecture-hover-dark-1440.png)
- [After Timeline progress](/C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31/experience-after/timeline-progress-dark-1440.png)

## 19. Real QA

`scripts/qa-experience-real.mjs` 通过 18/18 项体验交互检查，`consoleErrors=[]`，`failedRequests=[]`。既有 `scripts/qa-content-real.mjs` 通过 76 项内容、SEO、主题、Markdown 和多断点检查，控制台错误与请求失败同样为 0。

## 20. Removed Experiments

本轮没有保留临时实验代码。明确排除：sound feedback、custom cursor、粒子/星尘背景、WebGL、滚动锁死、自动播放媒体、虚构项目关系、重型 motion library。截图脚本仅存放在 Codex visualization workspace，不进入仓库产品代码。

## 21. After 最终评分

| 维度 | After | 变化 |
| --- | ---: | ---: |
| 视觉质量 | 90 | +4 |
| 品牌一致性 | 92 | +3 |
| 内容清晰度 | 89 | +2 |
| 交互完成度 | 84 | +26 |
| Motion 质量 | 82 | +40 |
| Accessibility | 86 | +10 |
| Responsive | 90 | +4 |
| Performance | 84 | +2 |
| 综合体验 | **87** | **+11** |

结论：Yu-log 已从“品牌视觉完成的公开前台”进入“有连续状态反馈、可探索、可键盘操作且尊重 Reduced Motion 的前端体验层”。下一阶段应按既定计划进入本地内容资产盘点，不继续扩展 Admin 功能。
