import { defineStore } from 'pinia';

export const useAppStore = defineStore('app', {
  state: () => ({
    title: 'YU.LOG',
    phase: 'engineering-skeleton',
  }),
});
