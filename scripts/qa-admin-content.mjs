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
page.on('dialog', async (dialog) => {
  if (dialog.type() === 'beforeunload') await dialog.accept();
});

function check(name, value) {
  checks.push({ name, pass: Boolean(value) });
  if (!value) throw new Error(`QA failed: ${name}`);
}

async function ready(path) {
  await page.goto(`${baseUrl}${path}`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(250);
}

async function noOverflow(name, targetPage = page) {
  check(`${name} has no horizontal overflow`, await targetPage.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1));
}

async function setTheme(targetPage, theme) {
  await targetPage.evaluate((value) => localStorage.setItem('yu-log-theme', value), theme);
  await targetPage.reload({ waitUntil: 'domcontentloaded' });
  await targetPage.waitForTimeout(220);
  check(`${theme} theme applied`, await targetPage.evaluate((value) => document.documentElement.dataset.theme === value, theme));
}

async function login() {
  await ready('/admin/login');
  await page.getByLabel('账号').fill('yu_admin');
  await page.getByLabel('密码').fill('Yu@123456');
  await page.getByRole('button', { name: '登录' }).click();
  await page.waitForURL(/\/admin(?:$|\?)/);
  await page.waitForTimeout(300);
}

try {
  await login();
  check('admin login reaches dashboard', await page.locator('.admin-header__title').textContent() === '控制台');
  check('dashboard renders real stat cards', await page.locator('.admin-queue-card').count() === 2 && await page.locator('.admin-shortcut').count() === 4);
  check('dashboard uses real pending queues', (await page.locator('.admin-queue-card').allTextContents()).every((text) => /待审核评论|待处理留言/.test(text)));
  await setTheme(page, 'dark');
  await page.screenshot({ path: `${outputDir}/wu6-admin-dashboard-dark-1440.png`, fullPage: true });

  await ready('/admin/articles/new');
  check('article editor renders shared markdown workspace', await page.locator('.admin-editor').count() === 1 && await page.locator('.admin-editor__tools button').count() >= 5);
  check('article editor exposes publish settings', await page.getByRole('heading', { name: '发布设置', exact: true }).count() === 1 && await page.getByRole('heading', { name: '标签', exact: true }).count() === 1);
  await page.locator('select').first().selectOption({ index: 1 });
  await page.getByLabel('标题 *').fill('Local Draft QA');
  await page.getByLabel('访问标识 slug *').fill('local-draft-qa');
  await page.locator('.admin-editor__input-wrap textarea').fill('## Draft\n\nThis is a local draft preview.');
  await page.getByRole('tab', { name: '预览' }).click();
  check('article editor preview uses public renderer', await page.locator('.admin-editor__preview .article-prose h2').count() === 1);
  await page.getByRole('tab', { name: '编辑', exact: true }).click();
  await page.waitForTimeout(450);
  check('article form autosaves a local draft', await page.evaluate(() => Boolean(localStorage.getItem('yu-log-admin-draft:article-new'))));
  await page.reload({ waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(250);
  check('article editor detects newer local draft', await page.getByText('发现较新的本地草稿').count() === 1);
  await page.getByRole('button', { name: '恢复' }).click();
  check('article editor restores local draft content', await page.locator('.admin-editor__input-wrap textarea').inputValue() === '## Draft\n\nThis is a local draft preview.');
  await page.getByRole('link', { name: '控制台', exact: true }).click();
  check('unsaved changes guard opens custom dialog', await page.getByRole('dialog').count() === 1 && await page.getByText('放弃未保存修改？').count() === 1);
  await page.getByRole('button', { name: '取消' }).click();
  check('unsaved changes guard can cancel navigation', page.url().endsWith('/admin/articles/new'));
  await page.close({ runBeforeUnload: false });

  const cleanPage = await context.newPage();
  cleanPage.on('console', (message) => { if (message.type() === 'error') consoleErrors.push(message.text()); if (message.type() === 'warning') consoleWarnings.push(message.text()); });
  cleanPage.on('pageerror', (error) => consoleErrors.push(`pageerror: ${error.message}`));
  cleanPage.on('requestfailed', (request) => failedRequests.push(`${request.method()} ${request.url()} ${request.failure()?.errorText || ''}`));
  await cleanPage.goto(`${baseUrl}/admin/notes/new`, { waitUntil: 'networkidle' });
  await cleanPage.waitForTimeout(250);
  await cleanPage.locator('.admin-editor').waitFor({ state: 'attached' });
  check('note editor reuses markdown workspace', await cleanPage.locator('.admin-editor').count() === 1 && await cleanPage.getByRole('heading', { name: '记录设置', exact: true }).count() === 1);
  await cleanPage.goto(`${baseUrl}/admin/projects/new`, { waitUntil: 'networkidle' });
  await cleanPage.waitForTimeout(250);
  await cleanPage.locator('.admin-editor').waitFor({ state: 'attached' });
  check('project editor reuses markdown workspace', await cleanPage.locator('.admin-editor').count() === 1 && await cleanPage.getByRole('heading', { name: '项目设置', exact: true }).count() === 1);
  await cleanPage.goto(`${baseUrl}/admin/timeline/new`, { waitUntil: 'networkidle' });
  await cleanPage.waitForTimeout(250);
  check('timeline type is a real select', await cleanPage.locator('select').count() >= 1 && await cleanPage.locator('select').first().locator('option').count() >= 2);
  await cleanPage.goto(`${baseUrl}/admin/site-settings`, { waitUntil: 'networkidle' });
  await cleanPage.waitForTimeout(350);
  check('site settings exposes structured profile form', await cleanPage.getByText('关于我资料').count() === 1 && await cleanPage.locator('.admin-structured-input').count() >= 1);
  check('site settings keeps advanced JSON scoped', await cleanPage.getByText('高级 JSON').count() <= 3 && (await cleanPage.locator('.admin-setting-card').allTextContents()).filter((text) => text.includes('高级 JSON')).length <= 1);
  await cleanPage.screenshot({ path: `${outputDir}/wu6-admin-site-settings-dark-1440.png`, fullPage: true });
  await cleanPage.goto(`${baseUrl}/admin/comments`, { waitUntil: 'networkidle' });
  await cleanPage.waitForTimeout(250);
  check('comments moderation page renders status filter', await cleanPage.locator('select').count() === 1 && await cleanPage.locator('option[value="PENDING"]').count() === 1);
  await cleanPage.goto(`${baseUrl}/admin/messages`, { waitUntil: 'networkidle' });
  await cleanPage.waitForTimeout(250);
  check('messages moderation page renders status filter', await cleanPage.locator('select').count() === 1 && await cleanPage.locator('option[value="PENDING"]').count() === 1);

  for (const path of ['/admin', '/admin/articles/new', '/admin/notes/new', '/admin/projects/new', '/admin/timeline/new', '/admin/site-settings', '/admin/comments', '/admin/messages']) {
    await cleanPage.goto(`${baseUrl}${path}`, { waitUntil: 'networkidle' });
    for (const width of [375, 430, 768, 1024, 1440]) {
      await cleanPage.setViewportSize({ width, height: 900 });
      await cleanPage.reload({ waitUntil: 'domcontentloaded' });
      await noOverflow(`${path} ${width}px`, cleanPage);
    }
  }
  await cleanPage.setViewportSize({ width: 375, height: 812 });
  await cleanPage.goto(`${baseUrl}/admin/articles/new`, { waitUntil: 'networkidle' });
  await cleanPage.waitForTimeout(250);
  await cleanPage.screenshot({ path: `${outputDir}/wu6-admin-article-mobile-375.png`, fullPage: true });
  await cleanPage.close({ runBeforeUnload: false });
} finally {
  await browser.close();
}

console.log(JSON.stringify({ checks, consoleErrors, consoleWarnings, failedRequests }, null, 2));
