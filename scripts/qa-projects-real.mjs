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

async function ready(path) {
  await page.goto(`${baseUrl}${path}`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(250);
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
  await ready('/projects');
  check('real projects index renders', await page.locator('h1').textContent() === 'Projects / Systems');
  check('real projects index returns two public rows', await page.locator('.project-row').count() === 2);
  check('real project stats reflect database rows', await page.locator('.projects-stats dd').first().textContent() === '2');
  check('placeholder repository link is hidden', await page.locator('a[href*="your-name"]').count() === 0);
  check('project detail links point to real IDs', await page.locator('.project-row a[href^="/projects/"]').count() === 2);
  check('project list title is SEO applied', await page.title() === 'Projects — Selected Systems | YU.LOG');
  await noOverflow('projects index desktop');
  await setTheme('dark');
  await page.screenshot({ path: `${outputDir}/wu4-projects-dark-1440.png`, fullPage: true });
  await setTheme('light');
  await page.screenshot({ path: `${outputDir}/wu4-projects-light-1440.png`, fullPage: true });

  await page.locator('.projects-field input').fill('YU.LOG');
  await page.locator('.projects-submit').click();
  await page.waitForTimeout(250);
  check('project search query is reflected in URL', new URL(page.url()).searchParams.get('q') === 'YU.LOG');
  check('project search returns one real project', await page.locator('.project-row').count() === 1);
  await page.locator('.projects-clear').click();
  await page.waitForTimeout(250);
  check('project filters can be reset', new URL(page.url()).pathname === '/projects' && !new URL(page.url()).searchParams.has('q'));

  await ready('/projects/1100');
  check('YU.LOG detail renders real title', await page.locator('h1').textContent() === 'YU.LOG 个人博客系统');
  check('YU.LOG detail renders real context', (await page.locator('.project-hero__description').textContent()).includes('Stitch'));
  check('YU.LOG detail renders truthful architecture', await page.locator('.project-architecture').count() === 1);
  check('YU.LOG detail renders truthful workflow', await page.locator('.project-workflow').count() === 1);
  check('YU.LOG detail renders real project notes', (await page.locator('.project-notes').textContent()).includes('后端开发'));
  check('YU.LOG detail hides placeholder links', await page.locator('a[href*="your-name"]').count() === 0);
  check('YU.LOG detail hides absent gallery', await page.locator('.project-gallery').count() === 0);
  check('YU.LOG detail has previous/next navigation', await page.locator('.project-navigation').count() === 1);
  check('YU.LOG detail has CreativeWork JSON-LD', await page.locator('script[type="application/ld+json"]').count() === 1);
  check('YU.LOG detail title is SEO applied', await page.title() === 'YU.LOG 个人博客系统 | Projects | YU.LOG');
  await noOverflow('YU.LOG detail desktop');
  await setTheme('dark');
  await page.screenshot({ path: `${outputDir}/wu4-project-detail-dark-1440.png`, fullPage: true });
  await setTheme('light');
  await page.screenshot({ path: `${outputDir}/wu4-project-detail-light-1440.png`, fullPage: true });

  await ready('/projects/1101');
  check('AI project detail renders real title', await page.locator('h1').textContent() === 'AI 学习助手实验');
  check('AI project detail renders real context', (await page.locator('.project-hero__description').textContent()).includes('课程复习'));
  check('unsupported architecture is omitted', await page.locator('.project-architecture').count() === 0);
  check('unsupported workflow is omitted', await page.locator('.project-workflow').count() === 0);
  check('AI project detail still renders real notes', (await page.locator('.project-notes').textContent()).includes('知识库'));
  check('AI project detail hides absent gallery', await page.locator('.project-gallery').count() === 0);
  await noOverflow('AI detail desktop');

  await ready('/projects');
  await page.locator('.projects-field select').nth(1).selectOption('COMPLETED');
  await page.waitForTimeout(250);
  check('status filter is reflected in URL', new URL(page.url()).searchParams.get('status') === 'COMPLETED');
  check('status filter returns the completed real project', await page.locator('.project-row').count() === 1 && (await page.locator('.project-row h2').textContent()).includes('AI'));
  await ready('/');
  const featuredLink = page.locator('.project-card').first().getByRole('link', { name: /View details/ });
  check('homepage featured project has case study link', await featuredLink.count() === 1);
  await featuredLink.click();
  await page.waitForURL(/\/projects\/1100$/);
  check('homepage featured project reaches matching detail', await page.locator('h1').textContent() === 'YU.LOG 个人博客系统');

  await page.setViewportSize({ width: 375, height: 812 });
  await setTheme('dark');
  await noOverflow('YU.LOG detail 375px dark');
  await page.screenshot({ path: `${outputDir}/wu4-project-detail-dark-375.png`, fullPage: true });
  await setTheme('light');
  await noOverflow('YU.LOG detail 375px light');
  await page.screenshot({ path: `${outputDir}/wu4-project-detail-light-375.png`, fullPage: true });

  for (const width of [430, 768, 1024, 1920]) {
    await page.setViewportSize({ width, height: 900 });
    await page.reload({ waitUntil: 'networkidle' });
    await noOverflow(`YU.LOG detail ${width}px`);
  }
} finally {
  await browser.close();
}

console.log(JSON.stringify({ checks, consoleErrors, failedRequests }, null, 2));
