import { defineStore } from 'pinia';

export type AdminToastKind = 'success' | 'error' | 'info';

interface ToastState {
  id: number;
  kind: AdminToastKind;
  message: string;
}

interface ConfirmState {
  id: number;
  title: string;
  message: string;
  confirmLabel: string;
  danger: boolean;
  resolve: (confirmed: boolean) => void;
}

interface PromptState {
  id: number;
  title: string;
  label: string;
  initialValue: string;
  confirmLabel: string;
  resolve: (value: string | null) => void;
}

let toastTimer: number | undefined;
let requestId = 0;

export const useAdminFeedbackStore = defineStore('admin-feedback', {
  state: () => ({
    toast: null as ToastState | null,
    confirmRequest: null as ConfirmState | null,
    promptRequest: null as PromptState | null,
  }),
  actions: {
    notify(message: string, kind: AdminToastKind = 'success') {
      this.toast = { id: ++requestId, kind, message };
      if (toastTimer) {
        window.clearTimeout(toastTimer);
      }
      toastTimer = window.setTimeout(() => {
        this.toast = null;
      }, 3600);
    },
    success(message: string) {
      this.notify(message, 'success');
    },
    error(message: string) {
      this.notify(message, 'error');
    },
    info(message: string) {
      this.notify(message, 'info');
    },
    dismissToast() {
      this.toast = null;
    },
    confirm(options: { title: string; message: string; confirmLabel?: string; danger?: boolean }): Promise<boolean> {
      return new Promise((resolve) => {
        this.confirmRequest = {
          id: ++requestId,
          title: options.title,
          message: options.message,
          confirmLabel: options.confirmLabel || '确认',
          danger: options.danger ?? false,
          resolve,
        };
      });
    },
    resolveConfirm(confirmed: boolean) {
      const request = this.confirmRequest;
      this.confirmRequest = null;
      request?.resolve(confirmed);
    },
    prompt(options: { title: string; label: string; initialValue?: string; confirmLabel?: string }): Promise<string | null> {
      return new Promise((resolve) => {
        this.promptRequest = {
          id: ++requestId,
          title: options.title,
          label: options.label,
          initialValue: options.initialValue || '',
          confirmLabel: options.confirmLabel || '保存回复',
          resolve,
        };
      });
    },
    resolvePrompt(value: string | null) {
      const request = this.promptRequest;
      this.promptRequest = null;
      request?.resolve(value);
    },
  },
});
