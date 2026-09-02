import DOMPurify from 'dompurify';
import hljs from 'highlight.js/lib/core';
import bash from 'highlight.js/lib/languages/bash';
import cpp from 'highlight.js/lib/languages/cpp';
import css from 'highlight.js/lib/languages/css';
import dockerfile from 'highlight.js/lib/languages/dockerfile';
import java from 'highlight.js/lib/languages/java';
import javascript from 'highlight.js/lib/languages/javascript';
import json from 'highlight.js/lib/languages/json';
import properties from 'highlight.js/lib/languages/properties';
import python from 'highlight.js/lib/languages/python';
import sql from 'highlight.js/lib/languages/sql';
import typescript from 'highlight.js/lib/languages/typescript';
import xml from 'highlight.js/lib/languages/xml';
import yaml from 'highlight.js/lib/languages/yaml';
import MarkdownIt from 'markdown-it';

import type { ArticleHeading, RenderedMarkdown } from '@/types/markdown';

hljs.registerLanguage('java', java);
hljs.registerLanguage('sql', sql);
hljs.registerLanguage('bash', bash);
hljs.registerLanguage('shell', bash);
hljs.registerLanguage('sh', bash);
hljs.registerLanguage('json', json);
hljs.registerLanguage('yaml', yaml);
hljs.registerLanguage('yml', yaml);
hljs.registerLanguage('xml', xml);
hljs.registerLanguage('html', xml);
hljs.registerLanguage('vue', xml);
hljs.registerLanguage('javascript', javascript);
hljs.registerLanguage('js', javascript);
hljs.registerLanguage('typescript', typescript);
hljs.registerLanguage('ts', typescript);
hljs.registerLanguage('css', css);
hljs.registerLanguage('cpp', cpp);
hljs.registerLanguage('c++', cpp);
hljs.registerLanguage('python', python);
hljs.registerLanguage('py', python);
hljs.registerLanguage('dockerfile', dockerfile);
hljs.registerLanguage('docker', dockerfile);
hljs.registerLanguage('properties', properties);

interface MarkdownEnvironment {
  [key: string]: unknown;
  headings: ArticleHeading[];
  slugCounts: Map<string, number>;
}

const languageAliases: Record<string, string> = {
  cxx: 'cpp',
  'c++': 'cpp',
  console: 'bash',
  docker: 'dockerfile',
  html: 'xml',
  js: 'javascript',
  jsx: 'javascript',
  py: 'python',
  shell: 'bash',
  sh: 'bash',
  ts: 'typescript',
  tsx: 'typescript',
  vue: 'vue',
  yml: 'yaml',
};

function normalizeLanguage(info: string): string {
  const language = info.trim().split(/\s+/)[0]?.toLowerCase() || 'text';
  return languageAliases[language] || language;
}

function slugifyHeading(value: string): string {
  const slug = value
    .normalize('NFKC')
    .toLowerCase()
    .trim()
    .replace(/[\s_]+/g, '-')
    .replace(/[^\p{Letter}\p{Number}\p{Script=Han}-]+/gu, '')
    .replace(/-{2,}/g, '-')
    .replace(/^-|-$/g, '');
  return slug || 'section';
}

function uniqueHeadingId(text: string, counts: Map<string, number>): string {
  const base = slugifyHeading(text);
  const current = counts.get(base) || 0;
  counts.set(base, current + 1);
  return current === 0 ? base : `${base}-${current + 1}`;
}

function escapeAttribute(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function highlightCode(code: string, language: string): string {
  if (!language || language === 'text' || language === 'plaintext' || !hljs.getLanguage(language)) {
    return MarkdownIt().utils.escapeHtml(code);
  }
  try {
    return hljs.highlight(code, { language, ignoreIllegals: true }).value;
  } catch {
    return MarkdownIt().utils.escapeHtml(code);
  }
}

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  breaks: false,
});

markdown.core.ruler.after('inline', 'task-list-items', (state) => {
  state.tokens.forEach((token, index) => {
    if (token.type !== 'inline' || index === 0 || state.tokens[index - 1]?.type !== 'paragraph_open') {
      return;
    }
    const listItem = state.tokens[index - 2];
    const firstChild = token.children?.[0];
    const match = firstChild?.type === 'text' ? firstChild.content.match(/^\[([ xX])\]\s+/) : null;
    if (!match || listItem?.type !== 'list_item_open' || !firstChild) {
      return;
    }

    listItem.attrJoin('class', 'task-list-item');
    firstChild.content = firstChild.content.slice(match[0].length);
    const checkbox = new state.Token('task_checkbox', '', 0);
    checkbox.meta = { checked: match[1]?.toLowerCase() === 'x' };
    token.children?.unshift(checkbox);
  });
});

markdown.renderer.rules.task_checkbox = (tokens, index) => {
  const checked = Boolean(tokens[index]?.meta?.checked);
  return `<input class="task-list-checkbox" type="checkbox" disabled${checked ? ' checked' : ''} aria-label="${checked ? '已完成' : '未完成'}任务">`;
};

markdown.renderer.rules.fence = (tokens, index) => {
  const token = tokens[index];
  if (!token) return '';
  const language = normalizeLanguage(token.info || '');
  const label = language === 'text' ? '纯文本' : language;
  const highlighted = highlightCode(token.content, language);
  return `<div class="article-code-frame" data-code-block><div class="article-code-toolbar"><span class="article-code-language">${escapeAttribute(label)}</span><button class="article-code-copy" type="button" data-copy-code aria-label="复制 ${escapeAttribute(label)} 代码">复制</button></div><pre class="article-code-block"><code class="hljs language-${escapeAttribute(language)}">${highlighted}</code></pre></div>`;
};

markdown.renderer.rules.heading_open = (tokens, index, _options, environment) => {
  const token = tokens[index];
  if (!token) return '';
  const env = environment as unknown as MarkdownEnvironment;
  const sourceLevel = Number(token.tag.slice(1));
  const headingText = tokens[index + 1]?.content?.trim() || '小节';
  const headingId = uniqueHeadingId(headingText, env.slugCounts);
  const outputTag = sourceLevel === 1 ? 'h2' : token.tag;
  token.meta = { ...(token.meta || {}), headingId, outputTag };
  const closeToken = tokens.slice(index + 1).find((candidate) => candidate.type === 'heading_close' && candidate.tag === token.tag && !candidate.meta?.headingId);
  if (closeToken) closeToken.meta = { ...(closeToken.meta || {}), headingId, outputTag, headingText };
  if (sourceLevel >= 2 && sourceLevel <= 4) {
    env.headings.push({ id: headingId, text: headingText, level: sourceLevel });
  }
  const sourceClass = sourceLevel === 1 ? ' article-source-title' : '';
  return `<${outputTag} id="${escapeAttribute(headingId)}" class="article-heading${sourceClass}" data-article-heading>`;
};

markdown.renderer.rules.heading_close = (tokens, index) => {
  const token = tokens[index];
  const headingId = String(token?.meta?.headingId || 'section');
  const outputTag = String(token?.meta?.outputTag || token?.tag || 'h2');
  const headingText = String(token?.meta?.headingText || '此章节');
  return `<button class="article-heading-anchor" type="button" data-heading-anchor="${escapeAttribute(headingId)}" aria-label="复制${escapeAttribute(headingText)}章节链接">#</button></${outputTag}>`;
};

markdown.renderer.rules.table_open = () => '<div class="article-table-scroll" role="region" aria-label="可横向滚动的表格" tabindex="0"><table>';
markdown.renderer.rules.table_close = () => '</table></div>';

const defaultLinkOpen = markdown.renderer.rules.link_open || ((tokens, index, options, _env, renderer) => renderer.renderToken(tokens, index, options));
markdown.renderer.rules.link_open = (tokens, index, options, env, renderer) => {
  const token = tokens[index];
  const href = String(token?.attrGet('href') || '');
  if (/^https?:\/\//i.test(href)) {
    token?.attrSet('target', '_blank');
    token?.attrSet('rel', 'noopener noreferrer');
  }
  return defaultLinkOpen(tokens, index, options, env, renderer);
};

const defaultImage = markdown.renderer.rules.image || ((tokens, index, options, _env, renderer) => renderer.renderToken(tokens, index, options));
markdown.renderer.rules.image = (tokens, index, options, env, renderer) => {
  const token = tokens[index];
  token?.attrSet('loading', 'lazy');
  token?.attrSet('decoding', 'async');
  return defaultImage(tokens, index, options, env, renderer);
};

export function renderMarkdown(source: string): RenderedMarkdown {
  const environment: MarkdownEnvironment = { headings: [], slugCounts: new Map() };
  const rendered = markdown.render(source || '', environment as unknown as Parameters<typeof markdown.render>[1]);
  const html = DOMPurify.sanitize(rendered, {
    ALLOWED_TAGS: [
      'a', 'blockquote', 'br', 'button', 'code', 'del', 'div', 'em', 'h2', 'h3', 'h4', 'h5', 'h6',
      'hr', 'img', 'input', 'li', 'ol', 'p', 'pre', 'span', 'strong', 'table', 'tbody', 'td', 'th', 'thead', 'tr', 'ul',
    ],
    ALLOWED_ATTR: [
      'alt', 'aria-label', 'checked', 'class', 'data-article-heading', 'data-code-block', 'data-copy-code',
      'data-heading-anchor', 'decoding', 'disabled', 'href', 'id', 'loading', 'rel', 'role', 'src', 'tabindex',
      'target', 'title', 'type',
    ],
    ALLOW_ARIA_ATTR: true,
    ALLOW_DATA_ATTR: true,
    FORBID_TAGS: ['iframe', 'object', 'embed', 'form', 'style', 'script'],
    FORBID_ATTR: ['style'],
  });
  return { html: String(html), headings: environment.headings };
}
