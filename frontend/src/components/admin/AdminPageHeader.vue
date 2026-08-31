<script setup lang="ts">
defineProps<{
  eyebrow: string;
  title: string;
  description?: string;
}>();
</script>

<template>
  <div class="admin-page-header">
    <div>
      <p class="admin-eyebrow">{{ eyebrow }}</p>
      <h2 class="admin-page-header__title">{{ title }}</h2>
      <p v-if="description" class="admin-page-header__description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="admin-page-header__actions"><slot name="actions" /></div>
  </div>
</template>
