import { chromium } from 'file:///C:/Users/sxl-0/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs';
import { writeFile } from 'node:fs/promises';

const baseUrl = 'http://localhost:5173';
const outputDir = 'C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31';
const widths = [375, 430, 768, 1024, 1440, 1920];
const routes = [
  { path: '/', heading: '你好' },
  { path: '/articles', heading: '文章' },
  { path: '/articles/1001', heading: 'MySQL 与 Redis' },
  { path: '/projects', heading: '项目' },
  { path: '/projects/1100', heading: 'YU.LOG 个人博客系统' },
  { path: '/notes', heading: '正在生长' },
  { path: '/notes/1200', heading: 'Spring Boot' },
  { path: '/timeline', heading: '一路留下' },
  { path: '/about', heading: '系统背后' },
  { path: '/messages', heading: '留言板' },
  { path: '/polish-route-not-found', heading: '这里还没有' },
  { path: '/admin/login', heading: '管理员登录' },
];

const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe' });
const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
const page = await context.newPage();
const checks = [];
const consoleErrors = [];
const failedRequests = [];

page.on('console', (message) => { if (message.type() === 'error') consoleErrors.push(message.text()); });
page.on('pageerror', (error) => consoleErrors.push('pageerror: ' + error.message));
page.on('requestfailed', (request) => failedRequests.push(request.method() + ' ' + request.url() + ' ' + (request.failure()?.errorText || '')));

function check(name, value, detail = '') {
  checks.push({ name, pass: Boolean(value), ...(detail ? { detail } : {}) });
}

async function ready(path) {
  await page.goto(baseUrl + path, { waitUntil: 'domcontentloaded', timeout: 15000 });
  await page.waitForTimeout(420);
}

async function noOverflow(name) {
  const dimensions = await page.evaluate(() => ({
    viewport: window.innerWidth,
    scrollWidth: document.documentElement.scrollWidth,
  }));
  check(name + ' 没有横向溢出', dimensions.scrollWidth <= dimensions.viewport + 1, JSON.stringify(dimensions));
}

try {
  for (const route of routes) {
    for (const width of widths) {
      await page.setViewportSize({ width, height: width <= 430 ? 812 : 900 });
      await ready(route.path);
      if (width === 1440) {
        const heading = (await page.locator('h1').first().textContent().catch(() => '')) || '';
        check(route.path + ' 中文主标题存在', heading.includes(route.heading), heading);
        check(route.path + ' 使用中文文档语言', await page.evaluate(() => document.documentElement.lang === 'zh-CN'));
      }
      await noOverflow(route.path + ' @ ' + width + 'px');
    }
  }

  await page.setViewportSize({ width: 1440, height: 900 });
  await ready('/');
  check('主页使用中文优先导航', await page.locator('nav[aria-label="主要导航"]').innerText().then((value) => ['首页', '文章', '项目', '笔记', '成长', '留言', '关于'].every((label) => value.includes(label))));
  check('中文系统字体栈已生效', await page.evaluate(() => getComputedStyle(document.body).fontFamily.includes('PingFang SC')));
  check('Hero 中文动态文字可访问', await page.locator('.kinetic-text .sr-only').first().textContent().then((value) => Boolean(value?.trim())));

  await page.keyboard.press('Control+k');
  await page.locator('.command-palette').waitFor({ state: 'visible' });
  check('命令面板标题中文优先', await page.locator('#command-palette-title').innerText().then((value) => value.startsWith('搜索这片数字花园')));
  check('命令面板搜索提示中文优先', await page.locator('.command-palette__input-wrap input').getAttribute('placeholder').then((value) => value === '搜索文章、项目和笔记……'));
  check('命令面板快捷键提示中文化', await page.locator('.command-palette__hint').innerText().then((value) => value.includes('选择') && value.includes('打开') && value.includes('关闭')));
  await page.keyboard.press('Escape');

  await page.setViewportSize({ width: 375, height: 812 });
  await ready('/');
  await page.locator('[aria-label="打开导航菜单"]').click();
  check('移动端抽屉使用中文导航', await page.locator('#mobile-navigation').innerText().then((value) => ['首页', '文章', '项目', '笔记', '成长', '留言', '关于'].every((label) => value.includes(label))));
  await page.locator('#mobile-navigation [aria-label="关闭导航菜单"]').click();

  const reducedContext = await browser.newContext({ viewport: { width: 1440, height: 900 }, reducedMotion: 'reduce' });
  const reducedPage = await reducedContext.newPage();
  await reducedPage.goto(baseUrl + '/', { waitUntil: 'domcontentloaded', timeout: 15000 });
  await reducedPage.waitForTimeout(420);
  check('Reduced Motion 下中文动态文字直接显示', await reducedPage.locator('.kinetic-text__phrase').first().evaluate((node) => {
    const style = getComputedStyle(node);
    return style.opacity === '1' && style.animationName === 'none' && style.filter === 'none';
  }));
  await reducedContext.close();

  await page.setViewportSize({ width: 1440, height: 900 });
  await ready('/admin');
  check('受保护 Admin 路由仍然跳转登录', new URL(page.url()).pathname === '/admin/login');
  check('Admin 登录页中文优先', await page.locator('body').innerText().then((value) => value.includes('管理员登录') && value.includes('账号') && value.includes('密码')));
} finally {
  await browser.close();
}

const result = {
  viewportWidths: widths,
  routes: routes.map((route) => route.path),
  checks,
  pass: checks.every((item) => item.pass) && failedRequests.length === 0,
  consoleErrors,
  failedRequests,
};
await writeFile(outputDir + '/chinese-first-qa.json', JSON.stringify(result, null, 2));
console.log(JSON.stringify(result, null, 2));
if (!result.pass) process.exitCode = 1;
