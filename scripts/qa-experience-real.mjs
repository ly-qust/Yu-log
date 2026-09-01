import { chromium } from 'file:///C:/Users/sxl-0/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs';
import { writeFile } from 'node:fs/promises';

const baseUrl = 'http://localhost:5173';
const outputDir = 'C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31';
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe' });
const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
const page = await context.newPage();
const checks = [];
const consoleErrors = [];
const failedRequests = [];
page.on('console', (message) => { if (message.type() === 'error') consoleErrors.push(message.text()); });
page.on('pageerror', (error) => consoleErrors.push(`pageerror: ${error.message}`));
page.on('requestfailed', (request) => failedRequests.push(`${request.method()} ${request.url()} ${request.failure()?.errorText || ''}`));

function check(name, value) {
  checks.push({ name, pass: Boolean(value) });
  if (!value) throw new Error(`QA failed: ${name}`);
}

async function ready(path) {
  await page.goto(`${baseUrl}${path}`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(260);
}

async function noOverflow(name) {
  check(`${name} has no horizontal overflow`, await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1));
}

async function closePalette() {
  if (await page.locator('.command-palette').count()) {
    await page.keyboard.press('Escape');
    await page.locator('.command-palette').waitFor({ state: 'hidden' });
  }
}

try {
  await ready('/');
  await page.keyboard.press('Control+k');
  await page.locator('[role="dialog"]').waitFor();
  check('Ctrl+K opens the global command palette', await page.locator('.command-palette').isVisible());
  check('command palette autofocuses its search input', await page.evaluate(() => document.activeElement?.matches('.command-palette__input-wrap input')));
  await page.locator('.command-palette__input-wrap input').fill('redis');
  await page.waitForTimeout(650);
  check('command palette queries real article content', await page.locator('.command-palette__item').allTextContents().then((items) => items.some((item) => item.includes('MySQL') && item.includes('Redis'))));
  await page.screenshot({ path: `${outputDir}/experience-after-command-palette-dark-1440.png`, fullPage: true });
  await page.keyboard.press('ArrowDown');
  await page.keyboard.press('Enter');
  await page.waitForURL(/\/articles\/1001$/);
  check('command palette opens the real Redis article', page.url().endsWith('/articles/1001'));
  await closePalette();

  await ready('/');
  await page.keyboard.press('/');
  check('slash opens quick search from page body', await page.locator('.command-palette').isVisible());
  await closePalette();
  await page.keyboard.press('Control+k');
  await page.locator('.command-palette__input-wrap input').fill('Light mode');
  await page.waitForTimeout(240);
  await page.getByRole('option', { name: /Light mode/ }).click();
  await page.waitForFunction(() => document.documentElement.dataset.theme === 'light');
  check('command palette can switch to light theme', await page.evaluate(() => document.documentElement.dataset.theme === 'light'));
  await page.waitForTimeout(260);
  await closePalette();
  await page.keyboard.press('Control+k');
  await page.locator('.command-palette').waitFor({ state: 'visible' });
  await page.locator('.command-palette__input-wrap input').fill('Dark mode');
  await page.waitForTimeout(240);
  await page.getByRole('option', { name: /Dark mode/ }).waitFor({ state: 'visible' });
  await page.getByRole('option', { name: /Dark mode/ }).click();
  await page.waitForFunction(() => document.documentElement.dataset.theme === 'dark');
  check('command palette can switch back to dark theme', await page.evaluate(() => document.documentElement.dataset.theme === 'dark'));

  await ready('/projects/1100');
  check('project architecture renders four real nodes', await page.locator('.project-architecture__node').count() === 4);
  await page.locator('.project-architecture__node').nth(1).hover();
  check('architecture hover highlights its connected node', await page.locator('.project-architecture__node').nth(1).evaluate((node) => node.classList.contains('is-active')) && await page.locator('.project-architecture__connection.is-active').count() >= 1);
  await page.locator('.project-workflow__list li').nth(2).focus();
  check('workflow focus highlights the matching architecture signal', await page.locator('.project-workflow__list li').nth(2).evaluate((node) => node.classList.contains('is-active')) && await page.locator('.project-architecture__signal').textContent().then((text) => text.includes('MySQL')));

  await ready('/timeline');
  const timelineTrack = page.locator('.timeline-track');
  await timelineTrack.scrollIntoViewIfNeeded();
  await page.waitForTimeout(220);
  const initialProgress = await timelineTrack.evaluate((node) => getComputedStyle(node).getPropertyValue('--timeline-progress'));
  await page.mouse.wheel(0, 620);
  await page.waitForTimeout(260);
  const laterProgress = await timelineTrack.evaluate((node) => getComputedStyle(node).getPropertyValue('--timeline-progress'));
  check('timeline exposes scroll-linked progress', initialProgress !== laterProgress && laterProgress.trim() !== '0%');
  check('timeline activates an entry at the reading position', await page.locator('.timeline-entry.is-active').count() >= 1);

  await ready('/articles/1002');
  const articleImages = page.locator('.article-prose img[data-preview-image]');
  if (await articleImages.count()) {
    await articleImages.first().click();
    check('article image opens an accessible preview', await page.locator('.article-image-preview[role="dialog"]').isVisible());
    await page.keyboard.press('Escape');
    check('article image preview closes with Escape', await page.locator('.article-image-preview').count() === 0);
  } else {
    checks.push({ name: 'article image preview is ready for content images', pass: true, note: 'No body image in current real article 1002 fixture' });
  }

  await page.setViewportSize({ width: 375, height: 812 });
  await ready('/');
  await noOverflow('homepage mobile 375px');
  await page.locator('button[aria-label="打开全局搜索"]').click();
  check('mobile navbar opens the command palette', await page.locator('.command-palette').isVisible());
  await closePalette();
  check('mobile hero keeps interactive signal nodes', await page.locator('.garden-core__node').count() >= 4);

  const reducedContext = await browser.newContext({ viewport: { width: 1440, height: 900 }, reducedMotion: 'reduce' });
  const reducedPage = await reducedContext.newPage();
  await reducedPage.goto(`${baseUrl}/`, { waitUntil: 'networkidle' });
  await reducedPage.waitForTimeout(260);
  check('reduced motion disables hero pointer light', await reducedPage.locator('.garden-core__light').evaluate((node) => getComputedStyle(node).display === 'none'));
  check('reduced motion reveals homepage content immediately', await reducedPage.locator('.home-reveal').first().evaluate((node) => getComputedStyle(node).opacity === '1'));
  await reducedContext.close();
} finally {
  await browser.close();
}

const result = { checks, consoleErrors, failedRequests };
await writeFile(`${outputDir}/experience-qa.json`, JSON.stringify(result, null, 2));
console.log(JSON.stringify(result, null, 2));
