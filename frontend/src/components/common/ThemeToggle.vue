<script setup lang="ts">
import BaseButton from './BaseButton.vue';
import { useThemeStore } from '@/stores/theme';

withDefaults(defineProps<{
  showLabel?: boolean;
}>(), {
  showLabel: false,
});

const themeStore = useThemeStore();

function toggleTheme(event: MouseEvent) {
  themeStore.cyclePreference(event.currentTarget instanceof HTMLElement ? event.currentTarget : undefined);
}
</script>

<template>
  <BaseButton
    :aria-label="'当前主题：' + themeStore.preferenceLabel + '，点击切换主题'"
    size="sm"
    :title="'主题：' + themeStore.preferenceLabel"
    variant="ghost"
    @click="toggleTheme"
  >
    <template #icon>
      <svg
        v-if="themeStore.preference === 'light'"
        aria-hidden="true"
        class="h-4 w-4"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <circle cx="12" cy="12" r="3.5" stroke-width="1.7" />
        <path d="M12 2.5v2M12 19.5v2M4.5 4.5l1.4 1.4m12.2 12.2 1.4 1.4M2.5 12h2m15 0h2M4.5 19.5l1.4-1.4M18.1 5.9l1.4-1.4" stroke-linecap="round" stroke-width="1.7" />
      </svg>
      <svg
        v-else-if="themeStore.preference === 'dark'"
        aria-hidden="true"
        class="h-4 w-4"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path d="M20 15.7A8.3 8.3 0 0 1 8.3 4a8.3 8.3 0 1 0 11.7 11.7Z" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.7" />
      </svg>
      <svg
        v-else
        aria-hidden="true"
        class="h-4 w-4"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <rect x="3" y="4.5" width="18" height="12" rx="2" stroke-width="1.7" />
        <path d="M9 20h6m-3-3.5V20" stroke-linecap="round" stroke-width="1.7" />
      </svg>
    </template>
    <span v-if="showLabel">{{ themeStore.preferenceLabel }}</span>
    <span v-else class="sr-only">{{ themeStore.preferenceLabel }}</span>
  </BaseButton>
</template>
