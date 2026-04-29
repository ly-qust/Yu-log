<template>
  <div class="min-h-screen">
    <PublicNav />
    <main class="mx-auto w-full max-w-7xl px-6 py-10">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import PublicNav from './PublicNav.vue';
</script>
