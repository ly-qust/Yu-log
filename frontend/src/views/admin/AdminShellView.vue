<script setup lang="ts">
import { useRouter } from 'vue-router';

import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const contentNavItems = [
  { label: '文章管理', to: '/admin/articles' },
  { label: '分类管理', to: '/admin/categories' },
  { label: '标签管理', to: '/admin/tags' },
];

async function logout() {
  await authStore.logout();
  await router.push('/admin/login');
}
</script>

<template>
  <div class="min-h-screen bg-cyber-base px-4 py-6 text-cyber-text md:px-6 md:py-8">
    <div class="mx-auto flex max-w-7xl flex-col gap-5 lg:flex-row">
      <aside class="glass-panel rounded-glass p-5 lg:sticky lg:top-6 lg:h-[calc(100vh-3rem)] lg:w-64">
        <div class="mb-6 flex items-center gap-2">
          <span class="h-3 w-3 rounded-full bg-red-400"></span>
          <span class="h-3 w-3 rounded-full bg-yellow-300"></span>
          <span class="h-3 w-3 rounded-full bg-emerald-400"></span>
        </div>

        <p class="terminal-label text-sm">admin_terminal</p>
        <h1 class="mt-3 font-display text-2xl font-semibold">后台终端</h1>
        <p class="mt-2 text-sm text-cyber-muted">
          {{ authStore.user?.nickname || authStore.user?.username }} / 管理员
        </p>

        <nav class="mt-8 grid gap-4">
          <RouterLink
            to="/admin"
            class="rounded-lg border border-transparent px-3 py-2 font-mono text-xs uppercase text-cyber-muted transition hover:border-cyber-cyan/50 hover:text-cyber-cyan"
            exact-active-class="border-cyber-cyan/70 bg-cyber-cyan/10 text-cyber-cyan"
          >
            控制台
          </RouterLink>

          <div>
            <p class="px-3 font-mono text-[11px] uppercase tracking-[0.18em] text-cyber-outline">内容管理</p>
            <div class="mt-2 grid gap-2 border-l border-cyber-border/70 pl-3">
              <RouterLink
                v-for="item in contentNavItems"
                :key="item.to"
                :to="item.to"
                class="rounded-lg border border-transparent px-3 py-2 font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan/50 hover:text-cyber-cyan"
                active-class="border-cyber-cyan/70 bg-cyber-cyan/10 text-cyber-cyan"
              >
                {{ item.label }}
              </RouterLink>
            </div>
          </div>
        </nav>

        <div class="mt-8 grid gap-3 lg:absolute lg:bottom-5 lg:left-5 lg:right-5">
          <RouterLink class="rounded-lg border border-cyber-border px-4 py-2 text-center font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan hover:text-cyber-cyan" to="/">
            返回首页
          </RouterLink>

          <button
            class="w-full rounded-lg border border-cyber-cyan/50 px-4 py-2 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base"
            type="button"
            @click="logout"
          >
            退出登录
          </button>
        </div>
      </aside>

      <main class="min-w-0 flex-1">
        <RouterView />
      </main>
    </div>
  </div>
</template>
