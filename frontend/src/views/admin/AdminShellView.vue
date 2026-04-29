<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { http } from '@/api/http';
import { useAuthStore } from '@/stores/auth';
import type { Result } from '@/types/api';

interface DashboardStats {
  articleCount: number;
  messageCount: number;
  projectCount: number;
  noteCount: number;
}

const router = useRouter();
const authStore = useAuthStore();
const stats = ref<DashboardStats | null>(null);

onMounted(async () => {
  const response = await http.get<Result<DashboardStats>>('/admin/dashboard');
  stats.value = response.data.data;
});

async function logout() {
  await authStore.logout();
  await router.push('/admin/login');
}
</script>

<template>
  <div class="min-h-screen bg-cyber-base px-6 py-8">
    <section class="glass-panel mx-auto max-w-7xl rounded-glass p-6">
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <p class="terminal-label text-sm">admin_terminal // skeleton</p>
          <h1 class="mt-4 font-display text-3xl font-semibold">系统控制台</h1>
          <p class="mt-3 text-cyber-muted">
            当前登录：{{ authStore.user?.nickname }} / {{ authStore.user?.roleCode }}
          </p>
        </div>

        <button
          class="rounded-lg border border-cyber-cyan/50 px-4 py-2 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base"
          type="button"
          @click="logout"
        >
          LOGOUT
        </button>
      </div>

      <div class="mt-8 grid gap-4 md:grid-cols-4">
        <div v-for="(value, key) in stats" :key="key" class="rounded-lg border border-cyber-border bg-cyber-bg/50 p-4">
          <p class="font-mono text-xs uppercase text-cyber-muted">{{ key }}</p>
          <p class="mt-2 font-display text-2xl font-semibold text-cyber-cyanBright">{{ value }}</p>
        </div>
      </div>

      <RouterLink class="mt-6 inline-block font-mono text-sm text-cyber-cyan" to="/">Return Home</RouterLink>
    </section>
  </div>
</template>
