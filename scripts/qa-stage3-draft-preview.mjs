import { readFile } from 'node:fs/promises';
import { chromium } from 'file:///C:/Users/sxl-0/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs';

const baseUrl = 'http://localhost:5173';
const draftPath = 'D:/project/Yu-log/docs/drafts/phase3-wu2-personal-blog-cms.md';
const outputPath = 'C:/Users/sxl-0/.codex/visualizations/2026/08/30/01a0530e-24d7-70e1-8dba-92b75b915d31/phase3-wu2-draft-preview-1440.png';
const draft = await readFile(draftPath, 'utf8');
const title = draft.match(/^# (.+)$/m)?.[1] || '阶段三 WU2 Draft';
const content = draft.replace(/^# .*?\n\n(?:>.*\n)+\n?/, '').trim();
const username = process.env.YU_LOG_QA_USERNAME;
const password = process.env.YU_LOG_QA_PASSWORD;
if (!username || !password) throw new Error('Set YU_LOG_QA_USERNAME and YU_LOG_QA_PASSWORD for the local preview; credentials are never stored in this script.');

const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe' });
const context = await browser.newContext({ viewport: { width: 1440, height: 900 }, reducedMotion: 'reduce' });
const page = await context.newPage();
const mutations = [];
page.on('request', (request) => {
  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(request.method())) mutations.push(`${request.method()} ${request.url()}`);
});

try {
  await page.goto(`${baseUrl}/admin/login`, { waitUntil: 'networkidle' });
  await page.getByLabel('账号').fill(username);
  await page.getByLabel('密码').fill(password);
  await page.getByRole('button', { name: '登录' }).click();
  await page.waitForURL('**/admin');
  mutations.length = 0;
  await page.goto(`${baseUrl}/admin/articles/new`, { waitUntil: 'networkidle' });
  await page.locator('.admin-editor').waitFor({ state: 'attached' });
  await page.getByLabel('标题 *').fill(title);
  await page.locator('.admin-editor__input-wrap textarea').fill(content);
  await page.getByRole('tab', { name: '预览', exact: true }).click();
  await page.locator('.admin-editor__preview .article-prose').waitFor({ state: 'visible' });
  const preview = {
    title,
    headingCount: await page.locator('.admin-editor__preview .article-prose h2, .admin-editor__preview .article-prose h3').count(),
    paragraphCount: await page.locator('.admin-editor__preview .article-prose p').count(),
    mutations,
  };
  if (preview.headingCount < 3 || preview.paragraphCount < 3) throw new Error('Draft preview did not render enough Markdown content.');
  if (mutations.length) throw new Error(`Draft preview made a server mutation: ${mutations.join(', ')}`);
  await page.screenshot({ path: outputPath, fullPage: true });
  console.log(JSON.stringify({ pass: true, outputPath, ...preview }, null, 2));
} finally {
  await browser.close();
}
