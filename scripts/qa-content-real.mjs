import { chromium } from 'file:///C:/Users/sxl-0/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs';

const baseUrl = 'http://localhost:5173';
const outputDir = 'C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31';
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe' });
const context = await browser.newContext({ viewport: { width: 1440, height: 900 }, reducedMotion: 'reduce' });
const page = await context.newPage();
const checks = [];
const consoleErrors = [];
const consoleWarnings = [];
const failedRequests = [];
page.on('console', (message) => {
  if (message.type() === 'error') consoleErrors.push(message.text());
  if (message.type() === 'warning') consoleWarnings.push(message.text());
});
page.on('pageerror', (error) => consoleErrors.push(`pageerror: ${error.message}`));
page.on('requestfailed', (request) => failedRequests.push(`${request.method()} ${request.url()} ${request.failure()?.errorText || ''}`));

function check(name, value) {
  checks.push({ name, pass: Boolean(value) });
  if (!value) throw new Error(`QA failed: ${name}`);
}

async function ready(path) {
  await page.goto(`${baseUrl}${path}`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(220);
}

async function noOverflow(name) {
  check(`${name} has no horizontal overflow`, await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1));
}

async function setTheme(theme) {
  await page.evaluate((value) => localStorage.setItem('yu-log-theme', value), theme);
  await page.reload({ waitUntil: 'networkidle' });
  await page.waitForTimeout(120);
  check(`${theme} theme applied`, await page.evaluate((value) => document.documentElement.dataset.theme === value, theme));
}

try {
  await ready('/about');
  check('about page renders real profile headline', (await page.locator('h1').textContent()).includes('系统背后'));
  check('about page renders real role', (await page.locator('.about-identity__role').textContent()).includes('计算机科学与技术本科生'));
  check('about page groups real skills', await page.locator('.about-skill-group').count() === 5);
  check('about page renders real education', (await page.locator('.about-record').allTextContents()).some((text) => text.includes('计算机科学与技术本科在读')));
  check('about page hides placeholder social link', await page.locator('a[href*="your-name"]').count() === 0);
  check('about page title is SEO applied', await page.title() === '关于我｜YU.LOG · Yu 的个人工程档案');
  await noOverflow('about desktop');
  await page.setViewportSize({ width: 1440, height: 900 });
  await setTheme('dark');
  await page.screenshot({ path: `${outputDir}/wu5-about-dark-1440.png`, fullPage: true });
  await setTheme('light');
  await page.screenshot({ path: `${outputDir}/wu5-about-light-1440.png`, fullPage: true });

  await ready('/timeline');
  check('timeline page renders five real records', await page.locator('.timeline-entry').count() === 5);
  check('timeline types are derived from real data', await page.locator('.timeline-field option').count() === 4);
  check('timeline exposes cleanup candidates without deleting them', (await page.locator('.timeline-results').textContent()).includes('6B测试时间线') && (await page.locator('.timeline-results').textContent()).includes('adfa'));
  check('timeline title is SEO applied', await page.title() === '成长轨迹｜YU.LOG · Yu 的成长记录');
  await page.locator('.timeline-field select').selectOption('PROJECT');
  await page.waitForTimeout(250);
  check('timeline type filter is reflected in URL', new URL(page.url()).searchParams.get('type') === 'PROJECT');
  check('timeline type filter returns two real project records', await page.locator('.timeline-entry').count() === 2);
  await page.locator('.timeline-reset').click();
  await page.waitForTimeout(250);
  check('timeline filter can be reset', !new URL(page.url()).searchParams.has('type'));
  await ready('/timeline');
  await page.setViewportSize({ width: 1440, height: 900 });
  await setTheme('dark');
  await page.screenshot({ path: `${outputDir}/wu5-timeline-dark-1440.png`, fullPage: true });
  await page.setViewportSize({ width: 375, height: 812 });
  await page.reload({ waitUntil: 'networkidle' });
  await noOverflow('timeline mobile 375px');
  await page.screenshot({ path: `${outputDir}/wu5-timeline-dark-375.png`, fullPage: true });

  await ready('/notes');
  check('notes page renders four real nodes', await page.locator('.note-node').count() === 4);
  check('note topics are derived from real data', await page.locator('.notes-field select option').count() === 4);
  check('notes display updated time', await page.locator('.note-node__head time').count() === 4);
  check('notes title is SEO applied', await page.title() === '笔记｜YU.LOG · Yu 的数字花园');
  const noteIds = await page.locator('.note-node a').evaluateAll((links) => links.map((link) => link.getAttribute('href')?.split('/').pop()).filter(Boolean));
  check('notes expose all real detail links', noteIds.length === 4 && noteIds.includes('1206'));
  await page.locator('.notes-field select').selectOption('TIL');
  await page.waitForTimeout(250);
  check('notes topic filter is reflected in URL', new URL(page.url()).searchParams.get('topic') === 'TIL');
  check('notes topic filter returns one real node', await page.locator('.note-node').count() === 1);
  await page.locator('.notes-clear').click();
  await page.waitForTimeout(250);
  check('notes filter can be reset', !new URL(page.url()).searchParams.has('topic'));
  await ready('/notes');
  await page.setViewportSize({ width: 1440, height: 900 });
  await setTheme('dark');
  await page.screenshot({ path: `${outputDir}/wu5-notes-dark-1440.png`, fullPage: true });
  await setTheme('light');
  await page.screenshot({ path: `${outputDir}/wu5-notes-light-1440.png`, fullPage: true });

  for (const id of noteIds) {
    await ready(`/notes/${id}`);
    check(`note ${id} detail renders a title`, Boolean((await page.locator('h1').textContent())?.trim()));
    check(`note ${id} detail uses shared Markdown renderer`, await page.locator('.article-prose').count() === 1 && await page.locator('.note-content pre').count() === 0);
    check(`note ${id} detail has dynamic SEO`, (await page.title()).endsWith('｜笔记｜YU.LOG') && await page.locator('link[rel="canonical"]').count() === 1);
    check(`note ${id} detail does not show an empty TOC`, await page.locator('.article-toc__mobile').count() === 0 || await page.locator('.article-toc__mobile ol li').count() > 0);
  }
  await ready('/notes/1200');
  await page.setViewportSize({ width: 1440, height: 900 });
  await setTheme('dark');
  await page.screenshot({ path: `${outputDir}/wu5-note-detail-dark-1440.png`, fullPage: true });
  await page.setViewportSize({ width: 375, height: 812 });
  await page.reload({ waitUntil: 'networkidle' });
  await noOverflow('note detail mobile 375px');
  await page.screenshot({ path: `${outputDir}/wu5-note-detail-dark-375.png`, fullPage: true });

  await ready('/');
  check('homepage links to About', await page.locator('.home-page a[href="/about"]').count() >= 1);
  check('homepage links to Timeline', await page.locator('.home-page a[href="/timeline"]').count() >= 1);
  check('homepage links to Notes index', await page.locator('.home-page a[href="/notes"]').count() >= 1);
  check('homepage garden nodes link to note details', await page.locator('.home-page a[href^="/notes/"]').count() >= 1);

  for (const path of ['/about', '/timeline', '/notes', '/notes/1200']) {
    await ready(path);
    for (const width of [375, 430, 768, 1024, 1440, 1920]) {
      await page.setViewportSize({ width, height: 900 });
      await page.reload({ waitUntil: 'networkidle' });
      await noOverflow(`${path} ${width}px`);
    }
  }
} finally {
  await browser.close();
}

console.log(JSON.stringify({ checks, consoleErrors, consoleWarnings, failedRequests }, null, 2));
