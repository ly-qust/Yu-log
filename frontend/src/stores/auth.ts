import { defineStore } from 'pinia';

import { fetchMeApi, loginApi, logoutApi } from '@/api/auth';
import type { LoginPayload, UserInfo } from '@/types/auth';

const ACCESS_TOKEN_KEY = 'yu_log_access_token';
const REFRESH_TOKEN_KEY = 'yu_log_refresh_token';
const USER_KEY = 'yu_log_user';

function readUser(): UserInfo | null {
  const raw = window.localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as UserInfo;
  } catch {
    return null;
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: window.localStorage.getItem(ACCESS_TOKEN_KEY) ?? '',
    refreshToken: window.localStorage.getItem(REFRESH_TOKEN_KEY) ?? '',
    user: readUser(),
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken),
    isAdmin: (state) => state.user?.roleCode === 'ADMIN',
  },
  actions: {
    async login(payload: LoginPayload) {
      const data = await loginApi(payload);
      this.accessToken = data.accessToken;
      this.refreshToken = data.refreshToken;
      this.user = data.user;
      window.localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);
      window.localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
      window.localStorage.setItem(USER_KEY, JSON.stringify(data.user));
    },
    async fetchMe() {
      const user = await fetchMeApi();
      this.user = user;
      window.localStorage.setItem(USER_KEY, JSON.stringify(user));
      return user;
    },
    async logout() {
      try {
        if (this.accessToken) {
          await logoutApi();
        }
      } finally {
        this.clear();
      }
    },
    clear() {
      this.accessToken = '';
      this.refreshToken = '';
      this.user = null;
      window.localStorage.removeItem(ACCESS_TOKEN_KEY);
      window.localStorage.removeItem(REFRESH_TOKEN_KEY);
      window.localStorage.removeItem(USER_KEY);
    },
  },
});
