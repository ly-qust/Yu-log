import { computed, ref } from 'vue';
import { defineStore } from 'pinia';

export type ThemePreference = 'light' | 'dark' | 'system';
export type ResolvedTheme = Exclude<ThemePreference, 'system'>;

const THEME_STORAGE_KEY = 'yu-log-theme';
const THEME_ORDER: ThemePreference[] = ['system', 'dark', 'light'];

function isThemePreference(value: string | null): value is ThemePreference {
  return value === 'light' || value === 'dark' || value === 'system';
}

function getSystemTheme(): ResolvedTheme {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

type TransitionDocument = Document & {
  startViewTransition?: (callback: () => void | Promise<void>) => { finished: Promise<void> };
};

function applyTheme(theme: ResolvedTheme, preference: ThemePreference) {
  const root = document.documentElement;
  root.dataset.theme = theme;
  root.dataset.themePreference = preference;
  root.style.colorScheme = theme;

  const themeColor = document.querySelector<HTMLMetaElement>('meta[name="theme-color"]');
  if (themeColor) {
    themeColor.content = theme === 'dark' ? '#0e1512' : '#f4f8f6';
  }
}

export const useThemeStore = defineStore('theme', () => {
  const preference = ref<ThemePreference>('system');
  const resolvedTheme = ref<ResolvedTheme>('dark');
  const initialized = ref(false);
  let mediaQuery: MediaQueryList | null = null;

  const preferenceLabel = computed(() => ({
    light: '浅色',
    dark: '深色',
    system: '跟随系统',
  })[preference.value]);

  function syncTheme() {
    resolvedTheme.value = preference.value === 'system' ? getSystemTheme() : preference.value;
    applyTheme(resolvedTheme.value, preference.value);
  }

  async function setPreference(nextPreference: ThemePreference, origin?: HTMLElement) {
    const transitionDocument = document as TransitionDocument;
    const canTransition = typeof transitionDocument.startViewTransition === 'function'
      && !window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (origin) {
      const rect = origin.getBoundingClientRect();
      document.documentElement.style.setProperty('--theme-origin-x', `${rect.left + rect.width / 2}px`);
      document.documentElement.style.setProperty('--theme-origin-y', `${rect.top + rect.height / 2}px`);
    }
    preference.value = nextPreference;
    window.localStorage.setItem(THEME_STORAGE_KEY, nextPreference);
    if (canTransition) {
      const transition = transitionDocument.startViewTransition!(() => syncTheme());
      await transition.finished.catch(() => undefined);
    } else {
      syncTheme();
    }
  }

  function cyclePreference(origin?: HTMLElement) {
    const currentIndex = THEME_ORDER.indexOf(preference.value);
    void setPreference(THEME_ORDER[(currentIndex + 1) % THEME_ORDER.length], origin);
  }

  function initialize() {
    if (initialized.value) {
      return;
    }

    const storedPreference = window.localStorage.getItem(THEME_STORAGE_KEY);
    preference.value = isThemePreference(storedPreference) ? storedPreference : 'system';
    mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    mediaQuery.addEventListener('change', () => {
      if (preference.value === 'system') {
        syncTheme();
      }
    });
    syncTheme();
    initialized.value = true;
  }

  return {
    preference,
    preferenceLabel,
    resolvedTheme,
    initialized,
    initialize,
    setPreference,
    cyclePreference,
  };
});
