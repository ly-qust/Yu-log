import { chromium } from 'file:///C:/Users/sxl-0/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs';

const baseUrl = 'http://127.0.0.1:5173';
const outputDir = 'C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31';
const longMarkdown = `# Building a reliable article reading surface

这是一篇用于验收阅读体验的长篇示例。它覆盖中文标题、段落、链接、任务列表、引用、表格、图片占位与多种代码块。

## Why reading flow matters

好的长文阅读体验需要让正文保持窄而稳定的节奏，同时让目录、进度和元信息在需要时出现。

### A smaller contract

把复杂能力拆成清晰的小组件，可以让文章内容本身保持专注。

#### Notes for later

这里还有一层标题用于验证 H4 的缩进。

- [x] 统一正文的行高与宽度
- [ ] 检查移动端横向滚动
- [ ] 将可访问性当成默认能力

> Content should feel calm, even when the system underneath is not.

### Implementation sketch

${'```'}ts
export function createReadingExperience(article: Article) {
  return { title: article.title, progress: 'scoped-to-content' };
}
${'```'}

同一篇文章也可能需要展示查询与配置：

${'```'}sql
SELECT title, published_at FROM articles WHERE status = 'PUBLISHED' ORDER BY published_at DESC;
${'```'}

${'```'}json
{ "theme": "quiet-cybernetic", "toc": true, "safeHtml": true }
${'```'}

### A compact table

| Layer | Responsibility | Signal |
| --- | --- | --- |
| Content | Markdown and metadata | readable |
| Navigation | outline and adjacent articles | oriented |
| Feedback | comments and appreciation | connected |

## Safety and details

正文中的 [外部链接](https://example.com) 应该在新标签页打开。下面这段 HTML 不应该执行：

<script>window.__unsafe = true</script>

### Keep going

阅读进度只计算文章正文，不把站点 Footer 计入分母。滚动到这里后，右侧目录应同步当前章节。

## Closing note

最后用一个小节收束这次验收，确保长页面的末尾仍有清晰的视觉终点。
`;

const articles = [
  { id: '1001', title: 'Building a calm reading surface', slug: 'calm-reading', summary: '关于长文阅读、信息层级和工程取舍的记录。', coverImage: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='160' height='120'%3E%3Crect width='160' height='120' fill='%23dceee8'/%3E%3Ccircle cx='120' cy='28' r='42' fill='%2300836f' fill-opacity='.22'/%3E%3C/svg%3E", categoryId: 'cat-1', categoryName: 'Engineering', tags: [{ id: 'tag-1', name: 'Frontend', slug: 'frontend' }], viewCount: 1280, likeCount: 36, commentCount: 2, readingTime: 8, isTop: true, publishedAt: '2026-08-20T08:00:00Z', updatedAt: '2026-08-22T08:00:00Z' },
  { id: '1002', title: 'Notes from a small system', slug: 'small-system', summary: '把日常经验整理为可以复用的系统。', coverImage: null, categoryId: 'cat-2', categoryName: 'Notes', tags: [{ id: 'tag-2', name: 'Systems', slug: 'systems' }], viewCount: 420, likeCount: 18, commentCount: 0, readingTime: 4, isTop: false, publishedAt: '2026-08-12T08:00:00Z', updatedAt: '2026-08-12T08:00:00Z' },
  { id: '1003', title: 'A field guide to useful defaults', slug: 'useful-defaults', summary: '关于默认值、边界和长期维护的一次复盘。', coverImage: null, categoryId: 'cat-1', categoryName: 'Engineering', tags: [{ id: 'tag-1', name: 'Frontend', slug: 'frontend' }], viewCount: 87, likeCount: 7, commentCount: 1, readingTime: 6, isTop: false, publishedAt: '2026-07-21T08:00:00Z', updatedAt: '2026-07-21T08:00:00Z' },
];

function pageResult(list = articles) {
  return { list, pageNum: 1, pageSize: 8, total: list.length, totalPages: 1, hasNext: false, hasPrevious: false };
}

async function mockApi(route) {
  const request = route.request();
  const url = new URL(request.url());
  const path = url.pathname.replace(/^.*\/api/, '');
  let data;
  if (path === '/about') data = { profile: { nickname: 'Yu', githubUrl: 'https://github.com/example', email: 'yu@example.com' }, skills: [], education: [] };
  else if (path === '/home/overview') data = { hero: { title: 'Yu', description: 'A digital garden.' }, stats: { articleCount: 3, projectCount: 0, noteCount: 0, messageCount: 0 }, latestArticles: [], featuredProjects: [], latestNotes: [], timelinePreview: [], currentlyLearning: [] };
  else if (path === '/categories') data = [{ id: 'cat-1', name: 'Engineering', slug: 'engineering', articleCount: 2 }, { id: 'cat-2', name: 'Notes', slug: 'notes', articleCount: 1 }];
  else if (path === '/tags') data = [{ id: 'tag-1', name: 'Frontend', slug: 'frontend', articleCount: 2 }, { id: 'tag-2', name: 'Systems', slug: 'systems', articleCount: 1 }];
  else if (path === '/articles/1001') data = { ...articles[0], content: longMarkdown };
  else if (path === '/articles/1002') data = { ...articles[1], content: '# A short article\n\nA short reading note.' };
  else if (path === '/articles/1003') data = { ...articles[2], content: '# Useful defaults\n\nA small note.' };
  else if (path === '/articles/1001/comments') data = [{ id: 'comment-1', nickname: 'Reader', content: 'This is a useful boundary.', createdAt: '2026-08-23T08:00:00Z', adminReply: 'Thank you for reading.', repliedAt: '2026-08-24T08:00:00Z' }];
  else if (path === '/articles/1002/comments' || path === '/articles/1003/comments') data = [];
  else if (path.startsWith('/articles/') && request.method() === 'POST') data = { likeCount: 37 };
  else if (path === '/articles') {
    const q = (url.searchParams.get('keyword') || url.searchParams.get('q') || '').toLowerCase();
    data = pageResult(q ? articles.filter((item) => item.title.toLowerCase().includes(q)) : articles);
  } else {
    await route.continue();
    return;
  }
  await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: 200, data, message: 'ok' }) });
}

const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe' });
const context = await browser.newContext({ viewport: { width: 375, height: 812 }, reducedMotion: 'reduce' });
const page = await context.newPage();
const consoleErrors = [];
const failedRequests = [];
page.on('console', (message) => { if (message.type() === 'error') consoleErrors.push(message.text()); });
page.on('requestfailed', (request) => { failedRequests.push(`${request.method()} ${request.url()} ${request.failure()?.errorText || ''}`); });
await page.route('**/api/**', mockApi);

const result = { checks: [], consoleErrors, failedRequests, screenshots: [] };
function check(name, value) { result.checks.push({ name, pass: Boolean(value) }); if (!value) throw new Error(`QA failed: ${name}`); }
async function noOverflow(name) { check(`${name} has no horizontal overflow`, await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)); }
async function screenshot(name) { const path = `${outputDir}/${name}.png`; await page.screenshot({ path, fullPage: true }); result.screenshots.push(path); }

await page.goto(`${baseUrl}/articles`, { waitUntil: 'networkidle' });
check('articles list renders', await page.locator('h1').textContent() === 'Writing');
check('articles list has three editorial rows', await page.locator('.article-row').count() === 3);
check('articles list renders optional cover support', await page.locator('.article-row__cover').count() === 1);
await noOverflow('articles mobile');
await screenshot('wu3-articles-375');
for (const width of [430, 768, 1024, 1920]) {
  await page.setViewportSize({ width, height: 900 });
  await page.goto(`${baseUrl}/articles`, { waitUntil: 'networkidle' });
  await noOverflow(`articles ${width}px`);
  await screenshot(`wu3-articles-${width}`);
}
await page.setViewportSize({ width: 375, height: 812 });
await page.goto(`${baseUrl}/articles`, { waitUntil: 'networkidle' });
await page.locator('input[type=search]').fill('calm');
await page.locator('.articles-submit').click();
await page.waitForTimeout(150);
check('filter state is reflected in URL', new URL(page.url()).searchParams.get('q') === 'calm');
check('filtered list renders one result', await page.locator('.article-row').count() === 1);

await page.setViewportSize({ width: 1440, height: 900 });
await page.goto(`${baseUrl}/articles/1001`, { waitUntil: 'networkidle' });
check('detail has one page heading', await page.locator('h1').count() === 1);
check('markdown paragraphs render as HTML', await page.locator('.article-prose p').count() >= 5);
check('markdown headings generate TOC entries', await page.locator('.article-toc a').count() >= 8);
check('code blocks are highlighted in framed containers', await page.locator('.article-code-frame').count() === 3);
check('task list renders disabled checkbox', await page.locator('.task-list-checkbox').count() === 3);
check('table is horizontally scroll-safe', await page.locator('.article-table-scroll').count() === 1);
check('unsafe script is not rendered', await page.locator('.article-prose script').count() === 0 && await page.evaluate(() => window.__unsafe !== true));
check('dynamic article title is applied', await page.title() === 'Building a calm reading surface | YU.LOG');
check('article JSON-LD is present', await page.locator('script[type="application/ld+json"]').count() >= 1);
await noOverflow('article desktop');
await screenshot('wu3-article-1440');
await page.locator('.article-toc-rail .article-toc__list a').first().click();
check('TOC navigation updates hash', new URL(page.url()).hash.length > 1);
await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight * 0.45));
await page.waitForTimeout(200);
check('reading progress advances', await page.locator('.reading-progress span').evaluate((element) => element.style.transform !== 'scaleX(0)'));
await page.setViewportSize({ width: 768, height: 900 });
await page.reload({ waitUntil: 'networkidle' });
check('mobile/tablet TOC is available', await page.locator('.article-toc__mobile').count() >= 1);
await noOverflow('article tablet');
await screenshot('wu3-article-768');
await page.setViewportSize({ width: 375, height: 812 });
await page.reload({ waitUntil: 'networkidle' });
await noOverflow('article mobile');
await screenshot('wu3-article-375');
await page.goto(`${baseUrl}/`, { waitUntil: 'networkidle' });
check('SEO cleanup restores shell title', await page.title() === 'Yu — Backend Developer & Digital Gardener | YU.LOG');
check('home metadata remains controlled by home view', await page.locator('meta[name="description"]').getAttribute('content') === 'A digital garden.');

await browser.close();
console.log(JSON.stringify(result, null, 2));
