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
          <main class="mt-6" tabindex="-1">
            <RouterView />
          </main>
        </div>
      </div>
    </div>

    <AdminFeedbackHost />
  </div>
</template>
