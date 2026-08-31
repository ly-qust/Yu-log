import { chromium } from 'file:///C:/Users/sxl-0/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs';

const baseUrl = 'http://localhost:5173';
const outputDir = 'C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31';
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe' });
const context = await browser.newContext({ viewport: { width: 1440, height: 900 }, reducedMotion: 'reduce' });
const page = await context.newPage();
const checks = [];
const consoleErrors = [];
const failedRequests = [];
page.on('console', (message) => { if (message.type() === 'error') consoleErrors.push(message.text()); });
page.on('requestfailed', (request) => { failedRequests.push(`${request.method()} ${request.url()} ${request.failure()?.errorText || ''}`); });

function check(name, value) {
  checks.push({ name, pass: Boolean(value) });
  if (!value) throw new Error(`QA failed: ${name}`);
}

async function noOverflow(name) {
  check(`${name} has no horizontal overflow`, await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1));
}

try {
  await page.goto(`${baseUrl}/articles`, { waitUntil: 'networkidle' });
  check('real articles list renders', await page.locator('h1').textContent() === 'Writing');
  console.log('real list rows=', await page.locator('.article-row').count(), 'body=', (await page.locator('body').innerText()).slice(0, 600));
  check('real API returns three public articles', await page.locator('.article-row').count() === 3);
  check('real list includes existing cover article', await page.locator('.article-row__cover').count() === 1);
  check('real list title is SEO applied', await page.title() === 'Writing — 技术文章 | YU.LOG');
  await noOverflow('real list desktop');
  await page.screenshot({ path: `${outputDir}/wu3-real-articles-1440.png`, fullPage: true });

  await page.locator('input[type=search]').fill('MySQL');
  await page.locator('.articles-submit').click();
  await page.waitForTimeout(250);
  check('real search query is reflected in URL', new URL(page.url()).searchParams.get('q') === 'MySQL');
  check('real search returns one article', await page.locator('.article-row').count() === 1);

  await page.goto(`${baseUrl}/articles/1001`, { waitUntil: 'networkidle' });
  check('real article detail title renders', await page.locator('h1').textContent() === 'MySQL 与 Redis 在博客系统中的职责边界');
  check('real Markdown content is rendered', await page.locator('.article-prose h2').count() >= 1 && await page.locator('.article-prose p').count() >= 1);
  check('real article metadata is applied', await page.title() === 'MySQL 与 Redis 在博客系统中的职责边界 | YU.LOG');
  check('real comments section renders', await page.locator('#discussion-title').count() === 1);
  check('real article has previous navigation when available', await page.locator('.article-navigation').count() === 1);
  await noOverflow('real article desktop');
  await page.screenshot({ path: `${outputDir}/wu3-real-article-1440.png`, fullPage: true });

  await page.goto(`${baseUrl}/articles/1002`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(600);
  check('real broken cover gets a visual fallback', await page.locator('.article-cover__fallback').count() === 1);
  await page.setViewportSize({ width: 375, height: 812 });
  await page.reload({ waitUntil: 'networkidle' });
  await noOverflow('real article mobile');
  check('real article without headings omits empty TOC', await page.locator('.article-toc').count() === 0);
  await page.screenshot({ path: `${outputDir}/wu3-real-article-375.png`, fullPage: true });
} finally {
  await browser.close();
}

console.log(JSON.stringify({ checks, consoleErrors, failedRequests }, null, 2));
