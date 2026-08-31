<script setup lang="ts">
import { reactive, ref, watch } from 'vue';

import type { CommentSubmitPayload, PublicComment } from '@/types/interaction';
import { formatDateTime } from '@/utils/format';

const props = withDefaults(defineProps<{
  comments?: PublicComment[];
  loading?: boolean;
  submitting?: boolean;
  error?: string;
  success?: string;
}>(), { comments: () => [], loading: false, submitting: false, error: '', success: '' });

const emit = defineEmits<{ refresh: []; submit: [payload: CommentSubmitPayload] }>();
const composerOpen = ref(false);
const validationMessage = ref('');
const form = reactive({ nickname: '', email: '', content: '' });

function resetForm() {
  form.nickname = '';
  form.email = '';
  form.content = '';
}

function submitForm() {
  validationMessage.value = '';
  if (!form.nickname.trim() || !form.content.trim()) {
    validationMessage.value = '请填写昵称和评论内容。';
    return;
  }
  emit('submit', { nickname: form.nickname.trim(), email: form.email.trim() || undefined, content: form.content.trim() });
}

watch(() => props.success, (value) => {
  if (value) {
    resetForm();
    composerOpen.value = false;
  }
});
</script>

<template>
  <section class="discussion" aria-labelledby="discussion-title">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="font-mono text-[0.65rem] uppercase tracking-[0.14em] text-brand">Discussion</p>
        <h2 id="discussion-title" class="mt-2 font-display text-3xl font-semibold text-text-primary">继续这篇讨论</h2>
        <p class="mt-2 max-w-xl text-sm leading-7 text-text-secondary">问题、补充和不同观点会在审核通过后公开展示。</p>
      </div>
      <button class="discussion__refresh" type="button" :disabled="loading" @click="emit('refresh')">{{ loading ? 'Loading…' : 'Refresh' }}</button>
    </div>

    <button v-if="!composerOpen" class="discussion__open" type="button" @click="composerOpen = true"><span>写下你的想法…</span><strong>Comment</strong></button>
    <form v-else class="discussion__form" @submit.prevent="submitForm">
      <div class="grid gap-4 sm:grid-cols-2">
        <label><span>昵称 *</span><input v-model="form.nickname" maxlength="30" autocomplete="name" type="text" /></label>
        <label><span>邮箱（不会公开）</span><input v-model="form.email" maxlength="128" autocomplete="email" type="email" /></label>
      </div>
      <label><span>评论内容 *</span><textarea v-model="form.content" maxlength="1000" rows="5"></textarea></label>
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p class="font-mono text-[0.65rem] text-text-muted">{{ form.content.length }} / 1000</p>
        <div class="flex gap-2">
          <button class="discussion__button" type="button" :disabled="submitting" @click="composerOpen = false; validationMessage = ''">取消</button>
          <button class="discussion__button is-primary" type="submit" :disabled="submitting">{{ submitting ? '提交中…' : '提交评论' }}</button>
        </div>
      </div>
    </form>

    <p v-if="validationMessage || error" class="discussion__message is-error" role="alert">{{ validationMessage || error }}</p>
    <p v-if="success" class="discussion__message is-success" role="status">{{ success }}</p>

    <div class="mt-8 border-t border-border-subtle/62">
      <div v-if="loading && comments.length === 0" class="py-8 font-mono text-xs text-text-muted">Loading discussion…</div>
      <div v-else-if="comments.length === 0" class="py-8 text-sm text-text-muted">这里还没有公开评论，欢迎留下第一条讨论。</div>
      <article v-for="comment in comments" v-else :key="comment.id" class="discussion__comment">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <h3 class="font-display text-base font-semibold text-text-primary">{{ comment.nickname }}</h3>
          <time class="font-mono text-[0.62rem] text-text-muted">{{ formatDateTime(comment.createdAt) }}</time>
        </div>
        <p class="mt-3 whitespace-pre-wrap break-words text-sm leading-7 text-text-secondary">{{ comment.content }}</p>
        <div v-if="comment.adminReply" class="discussion__reply">
          <p class="font-mono text-[0.62rem] uppercase tracking-[0.1em] text-brand">Yu replied · {{ formatDateTime(comment.repliedAt) }}</p>
          <p class="mt-2 whitespace-pre-wrap break-words text-sm leading-7 text-text-primary">{{ comment.adminReply }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.discussion { margin-top: 5rem; border-top: 1px solid rgb(var(--color-border-subtle) / .72); padding-top: 3rem; }
.discussion__refresh, .discussion__button { min-height: 2.5rem; border: 1px solid rgb(var(--color-border-subtle) / .75); border-radius: .55rem; padding: 0 .85rem; background: transparent; font-family: 'JetBrains Mono',monospace; font-size: .66rem; color: rgb(var(--color-text-secondary)); transition: border-color 180ms, color 180ms, background-color 180ms; }
.discussion__refresh:hover, .discussion__button:hover { border-color: rgb(var(--color-border-active) / .55); color: rgb(var(--color-brand-primary)); }
.discussion__open { display: flex; width: 100%; min-height: 3.5rem; align-items: center; justify-content: space-between; gap: 1rem; margin-top: 2rem; border: 1px solid rgb(var(--color-border-subtle) / .72); border-radius: .7rem; padding: 0 1rem; background: rgb(var(--color-surface-elevated) / .42); text-align: left; color: rgb(var(--color-text-muted)); }
.discussion__open strong { font-family: 'JetBrains Mono',monospace; font-size: .65rem; color: rgb(var(--color-brand-primary)); }
.discussion__form { display: grid; gap: 1rem; margin-top: 2rem; border: 1px solid rgb(var(--color-border-subtle) / .7); border-radius: .8rem; padding: 1rem; background: rgb(var(--color-surface-elevated) / .48); }
.discussion__form label { display: grid; gap: .42rem; }
.discussion__form label > span { font-family: 'JetBrains Mono',monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); }
.discussion__form input, .discussion__form textarea { width: 100%; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .55rem; padding: .7rem .8rem; background: rgb(var(--color-bg-primary) / .68); color: rgb(var(--color-text-primary)); outline: none; }
.discussion__form input:focus, .discussion__form textarea:focus { border-color: rgb(var(--color-brand-primary)); box-shadow: 0 0 0 3px rgb(var(--color-brand-primary) / .1); }
.discussion__button.is-primary { border-color: rgb(var(--color-brand-primary)); background: rgb(var(--color-brand-primary)); color: rgb(var(--color-brand-contrast)); }
.discussion__button:disabled, .discussion__refresh:disabled { cursor: wait; opacity: .5; }
.discussion__message { margin-top: 1rem; border-radius: .55rem; padding: .7rem .85rem; font-size: .8rem; }
.discussion__message.is-error { border: 1px solid rgb(var(--color-danger) / .35); background: rgb(var(--color-danger) / .08); color: rgb(var(--color-danger)); }
.discussion__message.is-success { border: 1px solid rgb(var(--color-success) / .35); background: rgb(var(--color-success) / .08); color: rgb(var(--color-success)); }
.discussion__comment { padding: 1.5rem 0; border-bottom: 1px solid rgb(var(--color-border-subtle) / .58); }
.discussion__reply { margin-top: 1rem; border-left: 2px solid rgb(var(--color-brand-primary) / .65); padding: .75rem 1rem; background: rgb(var(--color-brand-primary) / .05); }
</style>
