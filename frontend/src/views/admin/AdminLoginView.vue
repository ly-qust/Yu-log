<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const username = ref('yu_admin');
const password = ref('');
const loading = ref(false);
const errorMessage = ref('');

const canSubmit = computed(() => username.value.trim() && password.value.trim() && !loading.value);

async function submit() {
  if (!canSubmit.value) {
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  try {
    await authStore.login({
      username: username.value.trim(),
      password: password.value,
    });
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin';
    await router.push(redirect);
  } catch {
    errorMessage.value = '登录失败，请检查账号或密码。';
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center px-6 py-10">
    <section class="glass-panel w-full max-w-md rounded-glass p-6">
      <div class="mb-6 flex items-center gap-2">
        <span class="h-3 w-3 rounded-full bg-red-400"></span>
        <span class="h-3 w-3 rounded-full bg-yellow-300"></span>
        <span class="h-3 w-3 rounded-full bg-emerald-400"></span>
      </div>

      <p class="terminal-label text-sm">admin_terminal // authentication</p>
      <h1 class="mt-3 font-display text-3xl font-semibold text-cyber-text">管理员登录</h1>

      <form class="mt-8 space-y-5" @submit.prevent="submit">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">账号</span>
          <input
            v-model="username"
            class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none transition focus:border-cyber-cyan"
            autocomplete="username"
            type="text"
          />
        </label>

        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">密码</span>
          <input
            v-model="password"
            class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none transition focus:border-cyber-cyan"
            autocomplete="current-password"
            type="password"
          />
        </label>

        <p v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
          {{ errorMessage }}
        </p>

        <button
          class="w-full rounded-lg bg-cyber-cyanBright px-4 py-3 font-mono text-sm font-semibold text-cyber-base transition hover:bg-cyber-cyan disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="!canSubmit"
          type="submit"
        >
          {{ loading ? '验证中...' : '登录' }}
        </button>
      </form>
    </section>
  </main>
</template>
