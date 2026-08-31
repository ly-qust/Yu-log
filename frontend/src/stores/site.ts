import { computed, ref } from 'vue';
import { defineStore } from 'pinia';

import { fetchAbout } from '@/api/about';
import type { AboutData, AboutProfile } from '@/types/site';

function normalizeGithubUrl(value: unknown): string {
  if (typeof value !== 'string' || value.includes('your-name')) {
    return '';
  }
  try {
    const url = new URL(value.trim());
    const isGithub = url.hostname === 'github.com' || url.hostname === 'www.github.com';
    return isGithub && (url.protocol === 'https:' || url.protocol === 'http:') ? url.toString() : '';
  } catch {
    return '';
  }
}

function normalizeEmail(value: unknown): string {
  if (typeof value !== 'string') {
    return '';
  }
  const email = value.trim();
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) ? email : '';
}

export const useSiteStore = defineStore('site', () => {
  const about = ref<AboutData | null>(null);
  const profile = ref<AboutProfile | null>(null);
  const loaded = ref(false);
  const loading = ref(false);

  const githubUrl = computed(() => normalizeGithubUrl(profile.value?.githubUrl));
  const email = computed(() => normalizeEmail(profile.value?.email));

  async function loadPublicProfile() {
    if (loaded.value || loading.value) {
      return;
    }
    loading.value = true;
    try {
      const result = await fetchAbout();
      about.value = result;
      profile.value = result.profile || null;
    } catch {
      about.value = null;
      profile.value = null;
    } finally {
      loaded.value = true;
      loading.value = false;
    }
  }

  return {
    about,
    profile,
    githubUrl,
    email,
    loaded,
    loading,
    loadPublicProfile,
  };
});
