<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

import AdminFeedbackHost from '@/components/admin/AdminFeedbackHost.vue';
import AdminHeader from '@/components/admin/AdminHeader.vue';
import AdminSidebar from '@/components/admin/AdminSidebar.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const sidebarOpen = ref(false);

async function logout() {
  await authStore.logout();
  await router.push('/admin/login');
}
</script>

<template>
  <div class="admin-shell">
    <div class="admin-layout">
      <AdminSidebar :open="sidebarOpen" @close="sidebarOpen = false" />

      <div class="admin-content">
        <div class="admin-content__inner">
          <AdminHeader @menu="sidebarOpen = true" @logout="logout" />
          <div v-if="authStore.user?.mustChangePassword" class="mt-4 rounded-control border border-warning/45 bg-warning/10 px-4 py-3 text-sm text-warning" role="alert">
            当前管理员密码需要更新。<RouterLink class="font-semibold underline" to="/admin/account">立即修改密码</RouterLink>
          </div>
          <main class="mt-6" tabindex="-1">
            <RouterView />
          </main>
        </div>
      </div>
    </div>

    <AdminFeedbackHost />
  </div>
</template>
