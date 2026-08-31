<script setup lang="ts">
import { computed, nextTick, onUnmounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { fetchArticleDetail, fetchArticles, likeArticle } from '@/api/articles';
import { fetchArticleComments, submitArticleComment } from '@/api/comments';
import ArticleComments from '@/components/article/ArticleComments.vue';
import ArticleHeader from '@/components/article/ArticleHeader.vue';
import ArticleNavigation from '@/components/article/ArticleNavigation.vue';
import ArticleReadingProgress from '@/components/article/ArticleReadingProgress.vue';
import ArticleToc from '@/components/article/ArticleToc.vue';
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue';
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import { useSiteStore } from '@/stores/site';
import type { ArticleDetail, ArticleListItem } from '@/types/content';
import type { CommentSubmitPayload, PublicComment } from '@/types/interaction';
import type { ArticleHeading } from '@/types/markdown';
import { formatDate } from '@/utils/format';
import { applySeo } from '@/utils/seo';

const route = useRoute();
const siteStore = useSiteStore();
const article = ref<ArticleDetail | null>(null);
const comments = ref<PublicComment[]>([]);
const previousArticle = ref<ArticleListItem | null>(null);
const nextArticle = ref<ArticleListItem | null>(null);
const headings = ref<ArticleHeading[]>([]);
const activeHeadingId = ref('');
const articleContent = ref<HTMLElement | null>(null);
const loading = ref(false);
const commentsLoading = ref(false);
const submittingComment = ref(false);
const liking = ref(false);
const fatalError = ref('');
const commentsError = ref('');
const commentsSuccess = ref('');
const likeFeedback = ref('');
let requestSequence = 0;
let headingObserver: IntersectionObserver | null = null;
let cleanupSeo = () => {};
let likeFeedbackTimer = 0;

const articleId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});

const backTo = computed(() => {
  const back = window.history.state?.back;
  return typeof back === 'string' && /^\/articles(?:\?|$)/.test(back) ? back : '/articles';
});

function plainDescription(value: ArticleDetail): string {
  if (value.summary?.trim()) return value.summary.trim().slice(0, 180);
  return value.content
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[([^\]]+)\]\([^)]*\)/g, '$1')
    .replace(/[#>*_`~\-|]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, 180) || 'YU.LOG 技术文章。';
}

function applyArticleSeo(value: ArticleDetail) {
  cleanupSeo();
  const author = siteStore.profile?.nickname || 'Yu';
  const canonicalUrl = new URL(route.path, window.location.origin).toString();
  cleanupSeo = applySeo({
    title: `${value.title} | YU.LOG`,
    description: plainDescription(value),
    canonicalPath: route.path,
    type: 'article',
    image: value.coverImage || undefined,
    publishedTime: value.publishedAt,
    modifiedTime: value.updatedAt,
    author,
    structuredData: {
      '@context': 'https://schema.org',
      '@type': 'Article',
      headline: value.title,
      description: plainDescription(value),
      datePublished: value.publishedAt || undefined,
      dateModified: value.updatedAt || value.publishedAt || undefined,
      articleSection: value.categoryName || undefined,
      keywords: value.tags.map((tag) => tag.name).join(', '),
      image: value.coverImage ? new URL(value.coverImage, window.location.origin).toString() : undefined,
      author: { '@type': 'Person', name: author },
      mainEntityOfPage: { '@type': 'WebPage', '@id': canonicalUrl },
    },
  });
}

function resolveNavigation(items: ArticleListItem[], id: string) {
  const index = items.findIndex((item) => item.id === id);
  previousArticle.value = index > 0 ? items[index - 1] : null;
  nextArticle.value = index >= 0 && index < items.length - 1 ? items[index + 1] : null;
}

function scrollToInitialPosition() {
  if (route.hash) {
    const id = decodeURIComponent(route.hash.slice(1));
    const target = document.getElementById(id);
    if (target) {
      target.scrollIntoView({ behavior: 'auto', block: 'start' });
      activeHeadingId.value = id;
      return;
    }
  }
  window.scrollTo({ top: 0, behavior: 'auto' });
}

async function loadEntry() {
  const id = articleId.value;
  if (!id) {
    fatalError.value = '这篇文章不存在，或暂时没有公开。';
    return;
  }

  const requestId = ++requestSequence;
  loading.value = true;
  commentsLoading.value = true;
  fatalError.value = '';
  commentsError.value = '';
  commentsSuccess.value = '';
  likeFeedback.value = '';
  article.value = null;
  comments.value = [];
  headings.value = [];
  previousArticle.value = null;
  nextArticle.value = null;
  headingObserver?.disconnect();

  const profilePromise = siteStore.loadPublicProfile();
  const [detailResult, commentsResult, indexResult] = await Promise.allSettled([
    fetchArticleDetail(id),
    fetchArticleComments(id),
    fetchArticles({ page: 1, size: 100 }),
  ]);
  await profilePromise;

  if (requestId !== requestSequence) return;

  if (detailResult.status === 'rejected') {
    fatalError.value = '这篇文章不存在、已下线，或暂时无法读取。';
    loading.value = false;
    commentsLoading.value = false;
    return;
  }

  article.value = detailResult.value;
  if (commentsResult.status === 'fulfilled') comments.value = commentsResult.value;
  else commentsError.value = '评论暂时无法加载，正文阅读不受影响。';
  if (indexResult.status === 'fulfilled') resolveNavigation(indexResult.value.list, id);
  applyArticleSeo(detailResult.value);
  loading.value = false;
  commentsLoading.value = false;
  await nextTick();
  scrollToInitialPosition();
}

async function refreshComments() {
  const id = articleId.value;
  if (!id || commentsLoading.value) return;
  commentsLoading.value = true;
  commentsError.value = '';
  try {
    comments.value = await fetchArticleComments(id);
  } catch {
    commentsError.value = '评论暂时无法加载，请稍后再试。';
  } finally {
    commentsLoading.value = false;
  }
}

async function submitComment(payload: CommentSubmitPayload) {
  const id = articleId.value;
  if (!id || submittingComment.value) return;
  submittingComment.value = true;
  commentsError.value = '';
  commentsSuccess.value = '';
  try {
    const message = await submitArticleComment(id, payload);
    commentsSuccess.value = message || '评论已提交，审核通过后会出现在这里。';
    await refreshComments();
  } catch {
    commentsError.value = '评论提交失败，请检查内容后稍后再试。';
  } finally {
    submittingComment.value = false;
  }
}

async function likeCurrentArticle() {
  if (!article.value || liking.value) return;
  liking.value = true;
  likeFeedback.value = '';
  try {
    const likeCount = await likeArticle(article.value.id);
    article.value = { ...article.value, likeCount };
    likeFeedback.value = 'Thanks — 已收到';
  } catch {
    likeFeedback.value = '暂时未能同步，请稍后再试';
  } finally {
    liking.value = false;
    window.clearTimeout(likeFeedbackTimer);
    likeFeedbackTimer = window.setTimeout(() => { likeFeedback.value = ''; }, 2600);
  }
}

function setupHeadingObserver() {
  headingObserver?.disconnect();
  if (!headings.value.length || !articleContent.value) return;
  const elements = headings.value
    .map((heading) => document.getElementById(heading.id))
    .filter((element): element is HTMLElement => Boolean(element));
  if (!elements.length) return;

  activeHeadingId.value ||= elements[0].id;
  headingObserver = new IntersectionObserver(() => {
    const threshold = 132;
    const passed = elements.filter((element) => element.getBoundingClientRect().top <= threshold);
    activeHeadingId.value = (passed[passed.length - 1] || elements[0]).id;
  }, { rootMargin: '-96px 0px -72% 0px', threshold: [0, 1] });
  elements.forEach((element) => headingObserver?.observe(element));
}

async function updateToc(value: ArticleHeading[]) {
  headings.value = value;
  await nextTick();
  setupHeadingObserver();
}

function navigateHeading(id: string) {
  const target = document.getElementById(id);
  if (!target) return;
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  target.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'start' });
  activeHeadingId.value = id;
  const url = new URL(window.location.href);
  url.hash = id;
  window.history.replaceState(window.history.state, '', url);
}

watch(articleId, () => { void loadEntry(); }, { immediate: true });

onUnmounted(() => {
  requestSequence += 1;
  headingObserver?.disconnect();
  cleanupSeo();
  window.clearTimeout(likeFeedbackTimer);
});
</script>

<template>
  <PublicLayout>
    <ArticleReadingProgress v-if="article" :target="articleContent" />

    <div class="article-page">
      <div v-if="loading" class="article-loading" aria-label="文章加载中">
        <LoadingSkeleton :lines="2" />
        <div class="mt-12"><LoadingSkeleton :lines="6" /></div>
        <div class="mt-12"><LoadingSkeleton :lines="5" /></div>
      </div>

      <section v-else-if="fatalError" class="article-unavailable">
        <p class="font-mono text-[0.65rem] uppercase tracking-[0.15em] text-brand">Article unavailable</p>
        <h1>这一页暂时读不到</h1>
        <p>{{ fatalError }}</p>
        <div class="mt-7 flex flex-wrap gap-3">
          <button type="button" @click="loadEntry">重新加载</button>
          <RouterLink to="/articles">返回文章列表</RouterLink>
        </div>
      </section>

      <article v-else-if="article">
        <ArticleHeader :article="article" :back-to="backTo" :liking="liking" :like-feedback="likeFeedback" @like="likeCurrentArticle" />

        <div class="article-reading-grid">
          <div class="min-w-0">
            <ArticleToc class="mb-8 lg:hidden" :headings="headings" :active-id="activeHeadingId" @navigate="navigateHeading" />

            <div ref="articleContent" class="article-content">
              <MarkdownRenderer :content="article.content" @toc="updateToc" />
              <footer class="article-end">
                <span aria-hidden="true">◆</span>
                <p>Last revised {{ formatDate(article.updatedAt || article.publishedAt) }}</p>
              </footer>
            </div>

            <ArticleNavigation :previous="previousArticle" :next="nextArticle" />
            <ArticleComments
              :comments="comments"
              :loading="commentsLoading"
              :submitting="submittingComment"
              :error="commentsError"
              :success="commentsSuccess"
              @refresh="refreshComments"
              @submit="submitComment"
            />
          </div>

          <aside class="article-toc-rail hidden lg:block">
            <ArticleToc :headings="headings" :active-id="activeHeadingId" @navigate="navigateHeading" />
          </aside>
        </div>
      </article>
    </div>
  </PublicLayout>
</template>

<style scoped>
.article-page { width: min(100%,76rem); margin: 0 auto; padding-bottom: 6rem; }
.article-loading { width: min(100%,50rem); margin: 0 auto; padding: clamp(3rem,8vw,7rem) 0; }
.article-unavailable { width: min(100%,48rem); margin: 0 auto; padding: clamp(4rem,10vw,8rem) 0; }
.article-unavailable h1 { margin-top: 1rem; font-family: 'Space Grotesk',sans-serif; font-size: clamp(2.4rem,7vw,4.5rem); font-weight: 700; line-height: 1; letter-spacing: -.05em; color: rgb(var(--color-text-primary)); }
.article-unavailable > p:last-of-type { margin-top: 1.25rem; max-width: 34rem; line-height: 1.8; color: rgb(var(--color-text-secondary)); }
.article-unavailable button,.article-unavailable a { display: inline-flex; min-height: 2.7rem; align-items: center; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .55rem; padding: 0 .9rem; font-family: 'JetBrains Mono',monospace; font-size: .65rem; color: rgb(var(--color-text-secondary)); }
.article-unavailable button:hover,.article-unavailable a:hover { border-color: rgb(var(--color-brand-primary)); color: rgb(var(--color-brand-primary)); }
.article-reading-grid { display: grid; grid-template-columns: minmax(0,50rem) 13rem; justify-content: center; gap: clamp(2.5rem,5vw,4.5rem); }
.article-content { min-width: 0; }
.article-toc-rail { position: sticky; top: 7rem; align-self: start; }
.article-end { display: grid; justify-items: center; gap: .75rem; margin-top: 4rem; border-top: 1px solid rgb(var(--color-border-subtle) / .62); padding-top: 2.5rem; font-family: 'JetBrains Mono',monospace; font-size: .62rem; text-transform: uppercase; letter-spacing: .1em; color: rgb(var(--color-text-muted)); }
.article-end span { color: rgb(var(--color-brand-primary)); }
@media (max-width: 1023px) { .article-reading-grid { grid-template-columns: minmax(0,50rem); } }
@media (max-width: 639px) { .article-page { padding-bottom: 4rem; } }
</style>
