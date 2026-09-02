<script setup lang="ts">
import { computed, reactive, ref } from 'vue';

import { changePasswordApi } from '@/api/auth';
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import { useAuthStore } from '@/stores/auth';
import { getErrorMessage } from '@/utils/errors';

const authStore = useAuthStore();
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const form = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' });
const canSubmit = computed(() => Boolean(form.currentPassword && form.newPassword && form.confirmPassword && !loading.value));

async function submit() {
  errorMessage.value = '';
  successMessage.value = '';
  if (!canSubmit.value) return;
  if (form.newPassword.length < 12 || form.newPassword.length > 72) {
    errorMessage.value = '新密码长度需要在 12 到 72 位之间。';
    return;
  }
  if (form.newPassword !== form.confirmPassword) {
    errorMessage.value = '两次输入的新密码不一致。';
    return;
  }
  loading.value = true;
  try {
    const user = await changePasswordApi({ currentPassword: form.currentPassword, newPassword: form.newPassword });
    authStore.user = user;
    window.localStorage.setItem('yu_log_user', JSON.stringify(user));
    form.currentPassword = '';
    form.newPassword = '';
    form.confirmPassword = '';
    successMessage.value = '密码已更新。当前访问令牌仍会在原有效期内保持有效。';
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '密码修改失败，请稍后重试。');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="space-y-5">
    <AdminPageHeader eyebrow="账号安全 // SECURITY" title="账号安全" description="修改管理员密码需要验证当前密码；密码仅以 BCrypt 哈希形式保存。" />
    <form class="surface-muted max-w-2xl rounded-panel p-5 md:p-6" @submit.prevent="submit">
      <div v-if="errorMessage" class="admin-form-error" role="alert">{{ errorMessage }}</div>
      <div v-if="successMessage" class="admin-form-success" role="status">{{ successMessage }}</div>
      <div class="grid gap-4">
        <BaseInput v-model="form.currentPassword" label="当前密码" type="password" autocomplete="current-password" />
        <BaseInput v-model="form.newPassword" label="新密码" type="password" autocomplete="new-password" hint="12–72 位；请使用生产环境独有的强密码。" />
        <BaseInput v-model="form.confirmPassword" label="确认新密码" type="password" autocomplete="new-password" />
      </div>
      <div class="mt-5 flex justify-end"><BaseButton type="submit" :loading="loading" :disabled="!canSubmit">更新密码</BaseButton></div>
    </form>
  </section>
</template>

<style scoped>
.admin-form-error, .admin-form-success { border-radius: .65rem; padding: .75rem 1rem; font-size: .82rem; margin-bottom: 1rem; }
.admin-form-error { border: 1px solid rgb(var(--color-danger) / .4); background: rgb(var(--color-danger) / .08); color: rgb(var(--color-danger)); }
.admin-form-success { border: 1px solid rgb(var(--color-success) / .4); background: rgb(var(--color-success) / .08); color: rgb(var(--color-success)); }
</style>
