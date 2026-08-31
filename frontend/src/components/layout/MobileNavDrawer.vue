<script setup lang="ts">
import { nextTick, onUnmounted, ref, watch } from 'vue';

import BrandMark from '@/components/common/BrandMark.vue';
import IconButton from '@/components/common/IconButton.vue';
import ThemeToggle from '@/components/common/ThemeToggle.vue';
import { publicNavigation } from '@/mocks/navigation';

const props = withDefaults(defineProps<{
  open: boolean;
  githubUrl?: string;
}>(), {
  githubUrl: '',
});

const emit = defineEmits<{
  close: [];
}>();

const panelRef = ref<HTMLElement | null>(null);
let previouslyFocused: HTMLElement | null = null;
let previousBodyOverflow = '';

function close() {
  emit('close');
}

function getFocusableElements(): HTMLElement[] {
  if (!panelRef.value) {
    return [];
  }
  return Array.from(panelRef.value.querySelectorAll<HTMLElement>(
    'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
  )).filter((element) => !element.hasAttribute('hidden'));
}

function handleKeydown(event: KeyboardEvent) {
  if (!props.open) {
    return;
  }

  if (event.key === 'Escape') {
    event.preventDefault();
    close();
    return;
  }

  if (event.key !== 'Tab') {
    return;
  }

  const focusable = getFocusableElements();
  if (focusable.length === 0) {
    event.preventDefault();
    return;
  }

  const first = focusable[0];
  const last = focusable[focusable.length - 1];
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault();
    last.focus();
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault();
    first.focus();
  }
}

function unlockPageScroll() {
  document.body.style.overflow = previousBodyOverflow;
}

watch(
  () => props.open,
  async (open) => {
    if (open) {
      previouslyFocused = document.activeElement instanceof HTMLElement ? document.activeElement : null;
      previousBodyOverflow = document.body.style.overflow;
      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleKeydown);
      await nextTick();
      getFocusableElements()[0]?.focus();
      return;
    }

    window.removeEventListener('keydown', handleKeydown);
    unlockPageScroll();
    previouslyFocused?.focus();
    previouslyFocused = null;
  },
);

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown);
  unlockPageScroll();
});
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-normal ease-entrance"
      enter-from-class="opacity-0"
      leave-active-class="transition-opacity duration-fast ease-standard"
      leave-to-class="opacity-0"
    >
      <div v-if="open" class="fixed inset-0 z-[70] lg:hidden">
        <div
          aria-hidden="true"
          class="absolute inset-0 bg-overlay/58 backdrop-blur-sm"
          @click="close"
        ></div>

        <Transition
          appear
          enter-active-class="transition duration-slow ease-entrance"
          enter-from-class="translate-x-5 opacity-0"
          leave-active-class="transition duration-normal ease-standard"
          leave-to-class="translate-x-5 opacity-0"
        >
          <aside
            id="mobile-navigation"
            ref="panelRef"
            aria-label="移动端导航"
            aria-modal="true"
            class="absolute inset-y-0 right-0 flex w-[min(88vw,24rem)] flex-col border-l border-border-subtle bg-surface-elevated/95 p-5 shadow-elevated backdrop-blur-elevated"
            role="dialog"
          >
            <div class="flex items-center justify-between gap-4">
              <RouterLink aria-label="YU.LOG 首页" class="rounded-control" to="/" @click="close">
                <BrandMark />
              </RouterLink>
              <IconButton label="关闭导航菜单" @click="close">
                <svg aria-hidden="true" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path d="m6.5 6.5 11 11m0-11-11 11" stroke-linecap="round" stroke-width="1.7" />
                </svg>
              </IconButton>
            </div>

            <p class="mt-8 font-mono text-[0.6875rem] uppercase tracking-[0.16em] text-text-muted">
              Navigation // garden map
            </p>

            <nav aria-label="移动端主要导航" class="mt-3 grid gap-1.5">
              <RouterLink
                v-for="(item, index) in publicNavigation"
                :key="item.to"
                :active-class="item.exact ? '' : 'border-brand/24 bg-brand/10 text-brand'"
                class="group flex items-center justify-between rounded-control border border-transparent px-3.5 py-3 text-sm font-medium text-text-secondary transition duration-normal hover:border-border-subtle hover:bg-surface-hover/75 hover:text-text-primary"
                :exact-active-class="item.exact ? 'border-brand/24 bg-brand/10 text-brand' : ''"
                :to="item.to"
                @click="close"
              >
                <span>{{ item.label }}</span>
                <span class="font-mono text-[0.65rem] text-text-muted transition group-hover:text-brand">
                  {{ String(index + 1).padStart(2, '0') }}
                </span>
              </RouterLink>
            </nav>

            <div class="mt-auto border-t border-border-subtle/70 pt-5">
              <div class="flex items-center justify-between gap-3">
                <span class="font-mono text-xs text-text-muted">Theme</span>
                <ThemeToggle show-label />
              </div>

              <a
                v-if="githubUrl"
                :href="githubUrl"
                class="mt-3 flex items-center justify-between rounded-control border border-border-subtle bg-canvas-subtle/55 px-3.5 py-3 text-sm text-text-secondary transition hover:border-border-active/55 hover:text-brand"
                rel="noreferrer"
                target="_blank"
              >
                <span>GitHub</span>
                <svg aria-hidden="true" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path d="M8 16 16 8m-6 0h6v6" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.7" />
                </svg>
              </a>

              <p class="mt-5 font-mono text-[0.65rem] leading-5 text-text-muted">
                Building systems.<br />Growing a digital garden.
              </p>
            </div>
          </aside>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>
