import { chromium } from 'file:///C:/Users/sxl-0/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/index.mjs';

const baseUrl = 'http://localhost:5173';
const browser = await chromium.launch({
  headless: true,
  executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
});
const context = await browser.newContext({ viewport: { width: 1440, height: 900 }, reducedMotion: 'reduce' });
const page = await context.newPage();
const checks = [];
const consoleErrors = [];
const consoleWarnings = [];
const failedRequests = [];
const cleanupResults = [];
let runFailed = false;
const qa = {
  categoryId: null,
  tagId: null,
  articleId: null,
  noteId: null,
  timelineId: null,
  projectId: null,
  commentId: null,
  messageId: null,
  uploadUrl: null,
  uploadFilename: null,
};

page.on('console', (message) => {
  if (message.type() === 'error') consoleErrors.push(message.text());
  if (message.type() === 'warning') consoleWarnings.push(message.text());
});
page.on('pageerror', (error) => consoleErrors.push(`pageerror: ${error.message}`));
page.on('requestfailed', (request) => failedRequests.push(`${request.method()} ${request.url()} ${request.failure()?.errorText || ''}`));
page.on('dialog', async (dialog) => {
  if (dialog.type() === 'beforeunload') await dialog.accept();
});

function check(name, value, detail = '') {
  const pass = Boolean(value);
  checks.push({ name, pass, ...(detail ? { detail } : {}) });
  if (!pass) throw new Error(`QA failed: ${name}${detail ? ` — ${detail}` : ''}`);
}

async function ready(path) {
  await page.goto(`${baseUrl}${path}`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(350);
}

async function api(path, { method = 'GET', body } = {}) {
  return page.evaluate(async ({ path: requestPath, method: requestMethod, body: requestBody }) => {
    const token = window.localStorage.getItem('yu_log_access_token');
    const response = await fetch(`/api${requestPath}`, {
      method: requestMethod,
      headers: {
        ...(requestBody === undefined ? {} : { 'Content-Type': 'application/json' }),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: requestBody === undefined ? undefined : JSON.stringify(requestBody),
    });
    let parsed = null;
    try {
      parsed = await response.json();
    } catch {
      parsed = null;
    }
    return { status: response.status, body: parsed };
  }, { path, method, body });
}

function dataOf(response) {
  return response?.body?.data;
}

function responseMessage(response) {
  return response?.body?.message || `HTTP ${response?.status || 'unknown'}`;
}

function expectOk(response, name) {
  check(name, response.status >= 200 && response.status < 300 && response.body?.code === 0, responseMessage(response));
  return dataOf(response);
}

function expectStatus(response, status, name) {
  check(name, response.status === status, responseMessage(response));
}

function articlePayload(detail, overrides = {}) {
  return {
    title: detail.title,
    slug: detail.slug,
    summary: detail.summary || '',
    content: detail.content,
    coverImage: detail.coverImage || '',
    categoryId: detail.categoryId,
    tagIds: detail.tags.map((tag) => tag.id),
    status: detail.status,
    isTop: Boolean(detail.isTop),
    readingTime: detail.readingTime,
    ...overrides,
  };
}

async function login() {
  await ready('/admin/login');
  await page.getByLabel('账号').fill('yu_admin');
  await page.getByLabel('密码').fill('Yu@123456');
  await page.getByRole('button', { name: '登录' }).click();
  await page.waitForURL(/\/admin(?:$|\?)/);
  check('Admin login succeeds for write-chain test', page.url().includes('/admin'));
}

const articleContent = `# YU_LOG_QA_R3_ARTICLE\n\nA deterministic CMS write-chain article.\n\n## H2 section\n\n### H3 section\n\nJava code:\n\n\`\`\`java\npublic class QaArticle {\n    public static void main(String[] args) {\n        System.out.println("YU_LOG_QA");\n    }\n}\n\`\`\`\n\n| Field | Value |\n| --- | --- |\n| marker | YU_LOG_QA |\n\n> QA blockquote.\n\n[QA link](https://example.com) and \`inline-code\`.\n`;
let articleBodyEdited = `${articleContent}\n\n## Edited body\n\nYU_LOG_QA_R3_ARTICLE_BODY_EDITED\n`;
const articleBodyDiscarded = `${articleContent}\n\nYU_LOG_QA_R3_ARTICLE_LOCAL_ONLY\n`;

try {
  await login();

  const category = expectOk(await api('/admin/categories', {
    method: 'POST',
    body: {
      bizType: 'ARTICLE',
      name: 'YU_LOG_QA_R3_CATEGORY',
      slug: 'yu-log-qa-category-20260901-r3',
      description: 'Temporary QA category. Remove after write-chain test.',
      sortOrder: 9999,
      status: 'ENABLED',
    },
  }), 'create QA category');
  qa.categoryId = String(category.id);

  const tag = expectOk(await api('/admin/tags', {
    method: 'POST',
    body: {
      name: 'YU_LOG_QA_R3_TAG',
      slug: 'yu-log-qa-tag-20260901-r3',
      color: '#00e5ff',
      description: 'Temporary QA tag. Remove after write-chain test.',
      status: 'ENABLED',
    },
  }), 'create QA tag');
  qa.tagId = String(tag.id);

  await ready('/admin/articles/new');
  await page.getByLabel('标题 *').fill('YU_LOG_QA_R3_ARTICLE');
  await page.getByLabel('访问标识 slug *').fill('yu-log-qa-article-20260901-r3');
  await page.getByLabel('摘要').fill('YU_LOG_QA article summary for the real write-chain test.');
  await page.locator('select').first().selectOption(qa.categoryId);
  await page.locator(`input[type="checkbox"][value="${qa.tagId}"]`).check();
  await page.locator('.admin-editor__input-wrap textarea').fill(articleContent);
  await page.getByRole('tab', { name: 'Preview' }).click();
  check('article preview renders H2 and H3', await page.locator('.admin-editor__preview .article-prose h2').count() >= 2 && await page.locator('.admin-editor__preview .article-prose h3').count() >= 1);
  check('article preview renders code/table/blockquote/link/inline code', await page.locator('.admin-editor__preview pre code').count() >= 1 && await page.locator('.admin-editor__preview table').count() >= 1 && await page.locator('.admin-editor__preview blockquote').count() >= 1 && await page.locator('.admin-editor__preview a').count() >= 1 && await page.locator('.admin-editor__preview code').count() >= 2);
  await page.getByRole('tab', { name: 'Editor' }).click();

  const uploadResponsePromise = page.waitForResponse((response) => response.url().includes('/api/admin/files/upload') && response.request().method() === 'POST');
  await page.locator('.admin-editor input[type="file"]').setInputFiles({
    name: 'yu-log-qa-body.png',
    mimeType: 'image/png',
    buffer: Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=', 'base64'),
  });
  const uploadResponse = await uploadResponsePromise;
  const uploadData = expectOk(await uploadResponse.json().then((body) => ({ status: uploadResponse.status(), body })), 'upload QA Markdown image');
  qa.uploadUrl = uploadData.url;
  qa.uploadFilename = uploadData.filename;
  const uploadedArticleContent = await page.locator('.admin-editor__input-wrap textarea').inputValue();
  articleBodyEdited = `${uploadedArticleContent}\n\n## Edited body\n\nYU_LOG_QA_R3_ARTICLE_BODY_EDITED\n`;
  check('Markdown image upload inserts returned URL', uploadedArticleContent.includes(qa.uploadUrl));

  const invalidUploadResponsePromise = page.waitForResponse((response) => response.url().includes('/api/admin/files/upload') && response.request().method() === 'POST');
  await page.locator('.admin-editor input[type="file"]').setInputFiles({ name: 'yu-log-qa-invalid.txt', mimeType: 'text/plain', buffer: Buffer.from('not an image') });
  const invalidUploadResponse = await invalidUploadResponsePromise;
  expectStatus(await invalidUploadResponse.json().then((body) => ({ status: invalidUploadResponse.status(), body })), 400, 'invalid image type is rejected by API');
  check('invalid image type shows visible editor error', await page.locator('.admin-editor__error').isVisible());

  const createArticleResponsePromise = page.waitForResponse((response) => response.url().includes('/api/admin/articles') && response.request().method() === 'POST');
  await page.getByRole('button', { name: '保存草稿', exact: true }).click();
  const createArticleResponse = await createArticleResponsePromise;
  const createdArticle = expectOk(await createArticleResponse.json().then((body) => ({ status: createArticleResponse.status(), body })), 'save QA article draft');
  qa.articleId = String(createdArticle.id);
  await page.waitForURL(/\/admin\/articles$/);
  const draftArticleList = expectOk(await api(`/admin/articles?keyword=${encodeURIComponent('YU_LOG_QA_R3_ARTICLE')}&size=100`), 'reload QA article from admin list');
  check('saved QA article is a server-side draft', draftArticleList.list.some((item) => item.id === qa.articleId && item.status === 'DRAFT'));
  const publicDraftList = expectOk(await api(`/articles?keyword=${encodeURIComponent('YU_LOG_QA_R3_ARTICLE')}&size=100`), 'read public article list while draft');
  check('draft QA article is absent from public list', !publicDraftList.list.some((item) => item.id === qa.articleId));

  let articleServer = expectOk(await api(`/admin/articles/${qa.articleId}`), 'read QA article detail after draft save');
  const initialArticleSnapshot = { ...articleServer, tags: articleServer.tags.map((tagItem) => ({ ...tagItem })) };
  const coverUpdate = expectOk(await api(`/admin/articles/${qa.articleId}`, {
    method: 'PUT',
    body: articlePayload(articleServer, { coverImage: qa.uploadUrl, isTop: true, readingTime: 7 }),
  }), 'set QA article cover and top fields');
  check('QA article stores cover and top fields', coverUpdate.coverImage === qa.uploadUrl && coverUpdate.isTop === true && coverUpdate.readingTime === 7);
  articleServer = expectOk(await api(`/admin/articles/${qa.articleId}`), 're-read QA article fields');

  await ready(`/admin/articles/${qa.articleId}/edit`);
  const articleEditor = page.locator('.admin-editor__input-wrap textarea');
  await articleEditor.fill(articleBodyDiscarded);
  await page.waitForTimeout(900);
  const localDraftRaw = await page.evaluate(() => localStorage.getItem('yu-log-admin-draft:article-' + location.pathname.split('/')[3]));
  check('article local autosave stores unsaved body', Boolean(localDraftRaw) && JSON.parse(localDraftRaw).data.content.includes('YU_LOG_QA_R3_ARTICLE_LOCAL_ONLY'));
  await page.reload({ waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(400);
  check('article reload detects newer local draft', await page.getByText('发现较新的本地草稿').count() === 1);
  await page.getByRole('button', { name: '恢复' }).click();
  check('article local draft restore returns unsaved body', await articleEditor.inputValue() === articleBodyDiscarded);
  await articleEditor.fill(articleBodyDiscarded + '\nDiscard candidate');
  await page.waitForTimeout(850);
  await page.reload({ waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(400);
  await page.getByRole('button', { name: '丢弃' }).click();
  check('article local draft discard removes recovery banner', await page.getByText('发现较新的本地草稿').count() === 0 && await page.evaluate(() => localStorage.getItem('yu-log-admin-draft:article-' + location.pathname.split('/')[3]) === null));
  articleServer = expectOk(await api(`/admin/articles/${qa.articleId}`), 'verify discard did not write server');
  check('discarded local draft did not overwrite server content', articleServer.content === initialArticleSnapshot.content);

  await ready(`/admin/articles/${qa.articleId}/edit`);
  await page.getByRole('link', { name: 'Dashboard', exact: true }).click();
  await page.waitForURL(/\/admin$/);
  check('clean article navigation has no meaningless guard', await page.getByRole('dialog').count() === 0);
  await ready(`/admin/articles/${qa.articleId}/edit`);
  await articleEditor.fill(articleBodyEdited);
  await page.waitForTimeout(350);
  await page.getByRole('link', { name: 'Dashboard', exact: true }).click();
  check('unsaved internal navigation opens guard dialog', await page.getByRole('dialog').count() === 1 && await page.getByText('放弃未保存修改？').count() === 1);
  await page.getByRole('button', { name: '取消' }).click();
  check('guard cancel keeps article editor open', page.url().endsWith(`/admin/articles/${qa.articleId}/edit`));
  await page.getByRole('link', { name: 'Dashboard', exact: true }).click();
  await page.getByRole('button', { name: '离开页面' }).click();
  await page.waitForURL(/\/admin$/);
  check('guard confirm completes internal navigation', page.url().endsWith('/admin'));

  await ready(`/admin/articles/${qa.articleId}/edit`);
  if (await page.getByText('发现较新的本地草稿').count()) await page.getByRole('button', { name: '丢弃' }).click();
  await page.getByLabel('标题 *').fill(initialArticleSnapshot.title);
  await page.getByLabel('访问标识 slug *').fill(initialArticleSnapshot.slug);
  await articleEditor.fill(articleBodyEdited);
  const bodyEditResponsePromise = page.waitForResponse((response) => response.url().includes(`/api/admin/articles/${qa.articleId}`) && response.request().method() === 'PUT');
  await page.getByRole('button', { name: '保存草稿', exact: true }).click();
  const bodyEditResponse = await bodyEditResponsePromise;
  expectOk(await bodyEditResponse.json().then((body) => ({ status: bodyEditResponse.status(), body })), 'save article body-only edit');
  articleServer = expectOk(await api(`/admin/articles/${qa.articleId}`), 'read article after body-only edit');
  check('body-only edit preserves article fields', articleServer.content === articleBodyEdited && articleServer.categoryId === qa.categoryId && articleServer.tags.some((tagItem) => tagItem.id === qa.tagId) && articleServer.coverImage === qa.uploadUrl && articleServer.isTop === true && articleServer.status === 'DRAFT');

  await page.getByLabel('标题 *').fill('YU_LOG_QA_R3_ARTICLE_TITLE_EDITED');
  const titleEditResponsePromise = page.waitForResponse((response) => response.url().includes(`/api/admin/articles/${qa.articleId}`) && response.request().method() === 'PUT');
  await page.getByRole('button', { name: '保存草稿', exact: true }).click();
  const titleEditResponse = await titleEditResponsePromise;
  expectOk(await titleEditResponse.json().then((body) => ({ status: titleEditResponse.status(), body })), 'save article title-only edit');
  articleServer = expectOk(await api(`/admin/articles/${qa.articleId}`), 'read article after title-only edit');
  check('title-only edit preserves body/category/tags', articleServer.title === 'YU_LOG_QA_R3_ARTICLE_TITLE_EDITED' && articleServer.content === articleBodyEdited && articleServer.categoryId === qa.categoryId && articleServer.tags.some((tagItem) => tagItem.id === qa.tagId));

  const invalidCategoryResponse = await api(`/admin/articles/${qa.articleId}`, {
    method: 'PUT',
    body: articlePayload(articleServer, { categoryId: '999999999' }),
  });
  expectStatus(invalidCategoryResponse, 400, 'invalid category is rejected without write');
  const duplicateArticleResponse = await api('/admin/articles', {
    method: 'POST',
    body: articlePayload(articleServer, { title: 'YU_LOG_QA_R3_ARTICLE_DUPLICATE', slug: articleServer.slug }),
  });
  expectStatus(duplicateArticleResponse, 400, 'duplicate article slug is rejected');

  const publishResponsePromise = page.waitForResponse((response) => response.url().includes(`/api/admin/articles/${qa.articleId}`) && response.request().method() === 'PUT');
  await page.getByRole('button', { name: '发布文章', exact: true }).click();
  const publishResponse = await publishResponsePromise;
  const publishBody = await publishResponse.json();
  expectOk({ status: publishResponse.status(), body: publishBody }, 'publish QA article');
  articleServer = expectOk(await api(`/admin/articles/${qa.articleId}`), 'read published QA article');
  check('published QA article has published status', articleServer.status === 'PUBLISHED' && Boolean(articleServer.publishedAt));
  const publicArticleList = expectOk(await api(`/articles?keyword=${encodeURIComponent('YU_LOG_QA_R3_ARTICLE_TITLE_EDITED')}&size=100`), 'read published article from public API');
  check('published QA article appears in public API list', publicArticleList.list.some((item) => item.id === qa.articleId));
  const publicArticle = expectOk(await api(`/articles/${qa.articleId}`), 'read published article detail from public API');
  check('public article detail exposes title/body/category/tags', publicArticle.title === 'YU_LOG_QA_R3_ARTICLE_TITLE_EDITED' && publicArticle.content.includes('YU_LOG_QA_R3_ARTICLE_BODY_EDITED') && publicArticle.categoryName === 'YU_LOG_QA_R3_CATEGORY' && publicArticle.tags.some((tagItem) => tagItem.name === 'YU_LOG_QA_R3_TAG'));

  await ready(`/articles?q=${encodeURIComponent('YU_LOG_QA_R3_ARTICLE_TITLE_EDITED')}`);
  check('public article list renders QA article', (await page.locator('body').innerText()).includes('YU_LOG_QA_R3_ARTICLE_TITLE_EDITED'));
  await ready(`/articles/${qa.articleId}`);
  check('public article page renders full Markdown content', await page.locator('h1').filter({ hasText: 'YU_LOG_QA_R3_ARTICLE_TITLE_EDITED' }).count() === 1 && await page.locator('.article-prose h2').count() >= 2 && await page.locator('.article-prose h3').count() >= 1 && await page.locator('.article-prose pre code').count() >= 1 && await page.locator('.article-prose table').count() >= 1 && await page.locator('.article-prose blockquote').count() >= 1 && await page.locator('.article-prose a').count() >= 1 && await page.locator('.article-prose code').count() >= 2);
  check('public article page renders TOC/category/tag/SEO', await page.locator('.article-toc').count() >= 1 && (await page.locator('body').innerText()).includes('YU_LOG_QA_R3_CATEGORY') && (await page.locator('body').innerText()).includes('YU_LOG_QA_R3_TAG') && (await page.title()).includes('YU_LOG_QA_R3_ARTICLE_TITLE_EDITED') && Boolean(await page.locator('meta[name="description"]').getAttribute('content')) && (await page.locator('link[rel="canonical"]').getAttribute('href'))?.endsWith(`/articles/${qa.articleId}`));
  const articleImageSrc = await page.locator('.article-prose img').first().getAttribute('src');
  check('public article renders uploaded Markdown image', Boolean(articleImageSrc) && articleImageSrc === qa.uploadUrl);
  const articleImageResponse = await page.evaluate(async (src) => { const response = await fetch(src); return response.status; }, articleImageSrc);
  check('uploaded Markdown image is publicly readable', articleImageResponse === 200);

  const commentSubmit = expectOk(await api(`/articles/${qa.articleId}/comments`, { method: 'POST', body: { nickname: 'YU_LOG_QA_R3_COMMENT', email: 'yu-log-qa@example.com', content: 'YU_LOG_QA_R3_COMMENT_CONTENT' } }), 'submit QA comment');
  check('QA comment enters pending workflow', typeof commentSubmit === 'string');
  const commentList = expectOk(await api(`/admin/comments?keyword=${encodeURIComponent('YU_LOG_QA_R3_COMMENT')}&size=100`), 'find QA comment in admin');
  qa.commentId = commentList.list[0]?.id ? String(commentList.list[0].id) : null;
  check('admin can find QA comment', Boolean(qa.commentId));
  expectOk(await api(`/admin/comments/${qa.commentId}/status`, { method: 'PUT', body: { status: 'APPROVED' } }), 'approve QA comment');
  expectOk(await api(`/admin/comments/${qa.commentId}/reply`, { method: 'PUT', body: { adminReply: 'YU_LOG_QA_R3_COMMENT_REPLY' } }), 'reply to QA comment');

  const messageSubmit = expectOk(await api('/messages', { method: 'POST', body: { nickname: 'YU_LOG_QA_R3_MESSAGE', email: 'yu-log-qa@example.com', content: 'YU_LOG_QA_R3_MESSAGE_CONTENT' } }), 'submit QA message');
  check('QA message enters pending workflow', typeof messageSubmit === 'string');
  const messageList = expectOk(await api(`/admin/messages?keyword=${encodeURIComponent('YU_LOG_QA_R3_MESSAGE')}&size=100`), 'find QA message in admin');
  qa.messageId = messageList.list[0]?.id ? String(messageList.list[0].id) : null;
  check('admin can find QA message', Boolean(qa.messageId));
  expectOk(await api(`/admin/messages/${qa.messageId}/status`, { method: 'PUT', body: { status: 'APPROVED' } }), 'approve QA message');
  expectOk(await api(`/admin/messages/${qa.messageId}/reply`, { method: 'PUT', body: { adminReply: 'YU_LOG_QA_R3_MESSAGE_REPLY' } }), 'reply to QA message');

  expectOk(await api(`/admin/articles/${qa.articleId}/status`, { method: 'PUT', body: { status: 'HIDDEN' } }), 'hide QA article');
  const hiddenPublicList = expectOk(await api(`/articles?keyword=${encodeURIComponent('YU_LOG_QA_R3_ARTICLE_TITLE_EDITED')}&size=100`), 'read public list after hiding article');
  check('hidden QA article disappears from public list', !hiddenPublicList.list.some((item) => item.id === qa.articleId));
  const hiddenPublicDetail = await api(`/articles/${qa.articleId}`);
  check('hidden QA article is inaccessible publicly', hiddenPublicDetail.status === 400 && hiddenPublicDetail.body?.code === 404, responseMessage(hiddenPublicDetail));

  const note = expectOk(await api('/admin/notes', {
    method: 'POST',
    body: { title: 'YU_LOG_QA_R3_NOTE', slug: 'yu-log-qa-note-20260901-r3', summary: 'QA note summary', content: '## QA Note\n\nYU_LOG_QA_R3_NOTE_CONTENT', topic: 'QA', tags: ['YU_LOG_QA_R3_NOTE', 'workflow'], isPublic: true, sortOrder: 9999 },
  }), 'create QA note');
  qa.noteId = String(note.id);
  check('QA note is visible through public API', (await expectOk(await api(`/notes?keyword=${encodeURIComponent('YU_LOG_QA_R3_NOTE')}&size=100`), 'read QA note public list')).list.some((item) => item.id === qa.noteId));
  await ready(`/notes?q=${encodeURIComponent('YU_LOG_QA_R3_NOTE')}`);
  check('public notes page renders QA note', (await page.locator('body').innerText()).includes('YU_LOG_QA_R3_NOTE'));
  await ready(`/notes/${qa.noteId}`);
  check('public note detail renders Markdown', (await page.locator('body').innerText()).includes('YU_LOG_QA_R3_NOTE') && await page.locator('.article-prose h2').count() >= 1);
  const updatedNote = expectOk(await api(`/admin/notes/${qa.noteId}`, { method: 'PUT', body: { title: note.title, slug: note.slug, summary: note.summary, content: '## Edited QA Note\n\nYU_LOG_QA_R3_NOTE_EDITED_CONTENT', topic: note.topic, tags: note.tags, isPublic: note.isPublic, sortOrder: note.sortOrder } }), 'edit QA note');
  check('QA note edit persists', updatedNote.title === 'YU_LOG_QA_R3_NOTE' && updatedNote.content.includes('YU_LOG_QA_R3_NOTE_EDITED_CONTENT'));
  check('edited QA note is public', (await expectOk(await api(`/notes/${qa.noteId}`), 'read edited QA note publicly')).content.includes('YU_LOG_QA_R3_NOTE_EDITED_CONTENT'));

  const timeline = expectOk(await api('/admin/timeline', {
    method: 'POST',
    body: { title: 'YU_LOG_QA_R3_TIMELINE', description: 'YU_LOG_QA_R3_TIMELINE_DESCRIPTION', eventDate: '2026-09-01', type: 'MILESTONE', tags: ['YU_LOG_QA'], sortOrder: 9999, visible: true },
  }), 'create QA timeline event');
  qa.timelineId = String(timeline.id);
  check('QA timeline event appears in public filter', (await expectOk(await api('/timeline?type=MILESTONE&size=100'), 'read public timeline filter')).list.some((item) => item.id === qa.timelineId));
  await ready('/timeline?type=MILESTONE');
  check('public timeline page renders QA event', (await page.locator('body').innerText()).includes('YU_LOG_QA_R3_TIMELINE'));
  const updatedTimeline = expectOk(await api(`/admin/timeline/${qa.timelineId}`, { method: 'PUT', body: { title: 'YU_LOG_QA_R3_TIMELINE_EDITED', description: 'YU_LOG_QA_R3_TIMELINE_EDITED_DESCRIPTION', eventDate: timeline.eventDate, type: timeline.type, tags: timeline.tags, sortOrder: timeline.sortOrder, visible: timeline.visible } }), 'edit QA timeline event');
  check('QA timeline edit persists publicly', updatedTimeline.title === 'YU_LOG_QA_R3_TIMELINE_EDITED' && (await expectOk(await api('/timeline?type=MILESTONE&size=100'), 're-read edited timeline')).list.some((item) => item.id === qa.timelineId && item.description.includes('YU_LOG_QA_R3_TIMELINE_EDITED_DESCRIPTION')));

  const project = expectOk(await api('/admin/projects', {
    method: 'POST',
    body: { name: 'YU_LOG_QA_R3_PROJECT', slug: 'yu-log-qa-project-20260901-r3', description: 'QA project description', detailContent: '## QA Project\n\nYU_LOG_QA_R3_PROJECT_CONTENT', coverImage: '', techStack: ['Vue3', 'Spring Boot', 'YU_LOG_QA'], status: 'COMPLETED', githubUrl: 'https://github.com/ly-qust/Yu-log', demoUrl: 'https://example.com/yu-log-qa', sortOrder: 9999, visible: true },
  }), 'create QA project');
  qa.projectId = String(project.id);
  check('QA project appears in public list', (await expectOk(await api(`/projects?keyword=${encodeURIComponent('YU_LOG_QA_R3_PROJECT')}&size=100`), 'read QA project public list')).list.some((item) => item.id === qa.projectId));
  await ready(`/projects?q=${encodeURIComponent('YU_LOG_QA_R3_PROJECT')}`);
  check('public projects page renders QA project', (await page.locator('body').innerText()).includes('YU_LOG_QA_R3_PROJECT'));
  await ready(`/projects/${qa.projectId}`);
  check('public project detail renders SEO and Markdown', (await page.locator('body').innerText()).includes('YU_LOG_QA_R3_PROJECT') && await page.locator('.article-prose h2').count() >= 1 && (await page.title()).includes('YU_LOG_QA_R3_PROJECT') && Boolean(await page.locator('meta[name="description"]').getAttribute('content')));
  const updatedProject = expectOk(await api(`/admin/projects/${qa.projectId}`, { method: 'PUT', body: { name: project.name, slug: project.slug, description: project.description, detailContent: '## Edited QA Project\n\nYU_LOG_QA_R3_PROJECT_EDITED_CONTENT', coverImage: project.coverImage, techStack: project.techStack, status: project.status, githubUrl: project.githubUrl, demoUrl: project.demoUrl, sortOrder: project.sortOrder, visible: project.visible } }), 'edit QA project');
  check('QA project edit persists', updatedProject.detailContent.includes('YU_LOG_QA_R3_PROJECT_EDITED_CONTENT'));

  await ready('/admin/articles/new');
  await page.getByRole('button', { name: '保存草稿', exact: true }).click();
  check('missing article required fields show visible validation', await page.getByRole('alert').count() >= 1 && (await page.locator('body').innerText()).includes('请填写文章标题'));
  await page.getByLabel('标题 *').fill('YU_LOG_QA_R3_DUPLICATE_UI');
  await page.getByLabel('访问标识 slug *').fill('yu-log-qa-article-20260901-r3');
  await page.getByLabel('访问标识 slug *').press('Tab');
  await page.getByLabel('摘要').fill('duplicate UI validation');
  await page.locator('select').first().selectOption(qa.categoryId);
  await page.locator(`input[type="checkbox"][value="${qa.tagId}"]`).check();
  await page.locator('.admin-editor__input-wrap textarea').fill('YU_LOG_QA_DUPLICATE_UI_CONTENT');
  const duplicateUiResponsePromise = page.waitForResponse((response) => response.url().includes('/api/admin/articles') && response.request().method() === 'POST');
  await page.getByRole('button', { name: '保存草稿', exact: true }).click();
  const duplicateUiResponse = await duplicateUiResponsePromise;
  expectStatus(await duplicateUiResponse.json().then((body) => ({ status: duplicateUiResponse.status(), body })), 400, 'duplicate article slug shows API validation');
  check('duplicate article slug shows visible form error', await page.getByRole('alert').count() >= 1);

  await ready('/admin/site-settings');
  check('site settings loads structured form without write', await page.getByText('关于我资料').count() === 1);
  const advancedCard = page.locator('.admin-setting-card').filter({ has: page.getByText('Advanced JSON') }).first();
  check('site settings exposes scoped JSON validation field', await advancedCard.count() === 1);
  await advancedCard.locator('textarea').fill('{ invalid QA JSON');
  await advancedCard.getByRole('button', { name: '保存', exact: true }).click();
  check('invalid JSON settings show visible validation', (await page.getByRole('alert').count()) >= 1 && (await page.locator('body').innerText()).includes('不是合法 JSON'));
} catch (error) {
  runFailed = true;
  checks.push({ name: 'write-chain completed without uncaught failure', pass: false, detail: error instanceof Error ? error.message : String(error) });
  console.error(error instanceof Error ? error.stack : error);
} finally {
  try {
    const cleanup = async (label, path) => {
      const id = qa[label];
      if (!id) return;
      const response = await api(path.replace(':id', id), { method: 'DELETE' });
      const pass = response.status >= 200 && response.status < 300 && response.body?.code === 0;
      cleanupResults.push({ label, id, pass, ...(pass ? {} : { detail: responseMessage(response) }) });
    };
    await cleanup('commentId', '/admin/comments/:id');
    await cleanup('messageId', '/admin/messages/:id');
    await cleanup('articleId', '/admin/articles/:id');
    await cleanup('noteId', '/admin/notes/:id');
    await cleanup('timelineId', '/admin/timeline/:id');
    await cleanup('projectId', '/admin/projects/:id');
    await cleanup('categoryId', '/admin/categories/:id');
    await cleanup('tagId', '/admin/tags/:id');
  } catch (error) {
    cleanupResults.push({ label: 'cleanup-exception', pass: false, detail: error instanceof Error ? error.message : String(error) });
  }
  try {
    await browser.close();
  } catch {
    // Browser may already be closed after a successful run.
  }
}

const expectedConsoleErrors = consoleErrors.filter((message) => message.includes('status of 400'));
const unexpectedConsoleErrors = consoleErrors.filter((message) => !message.includes('status of 400'));
console.log(JSON.stringify({ checks, cleanupResults, qa: { ...qa, uploadUrl: qa.uploadUrl ? '[recorded]' : null }, consoleErrors: unexpectedConsoleErrors, expectedConsoleErrors, consoleWarnings, failedRequests }, null, 2));
if (runFailed || cleanupResults.some((result) => !result.pass)) process.exitCode = 1;
