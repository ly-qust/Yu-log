<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue';

import BaseButton from '@/components/common/BaseButton.vue';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';

const feedback = useAdminFeedbackStore();
const promptValue = ref('');
const promptInput = ref<HTMLTextAreaElement | null>(null);

watch(
  () => feedback.promptRequest?.id,
  async () => {
    promptValue.value = feedback.promptRequest?.initialValue || '';
    if (feedback.promptRequest) {
      await nextTick();
      promptInput.value?.focus();
    }
  },
  { immediate: true },
);

function handleKeydown(event: KeyboardEvent) {
  if (event.key !== 'Escape') {
    return;
  }
  if (feedback.promptRequest) {
    feedback.resolvePrompt(null);
  } else if (feedback.confirmRequest) {
    feedback.resolveConfirm(false);
  }
}

function submitPrompt() {
  feedback.resolvePrompt(promptValue.value);
}

onMounted(() => window.addEventListener('keydown', handleKeydown));
onUnmounted(() => window.removeEventListener('keydown', handleKeydown));
</script>

<template>
  <Transition name="admin-toast">
    <div
      v-if="feedback.toast"
      class="admin-toast"
      :class="`admin-toast--${feedback.toast.kind}`"
      role="status"
      aria-live="polite"
    >
      <span class="admin-toast__signal" aria-hidden="true"></span>
      <span>{{ feedback.toast.message }}</span>
      <button class="admin-toast__close" type="button" aria-label="关闭提示" @click="feedback.dismissToast">×</button>
    </div>
  </Transition>

  <div v-if="feedback.confirmRequest || feedback.promptRequest" class="admin-dialog-backdrop" @click.self="feedback.promptRequest ? feedback.resolvePrompt(null) : feedback.resolveConfirm(false)">
    <section v-if="feedback.confirmRequest" class="admin-dialog" role="dialog" aria-modal="true" :aria-labelledby="`confirm-title-${feedback.confirmRequest.id}`">
      <p class="admin-eyebrow">confirm // action required</p>
      <h2 :id="`confirm-title-${feedback.confirmRequest.id}`" class="admin-dialog__title">{{ feedback.confirmRequest.title }}</h2>
      <p class="admin-dialog__message">{{ feedback.confirmRequest.message }}</p>
      <div class="admin-dialog__actions">
        <BaseButton variant="secondary" @click="feedback.resolveConfirm(false)">取消</BaseButton>
        <BaseButton :variant="feedback.confirmRequest.danger ? 'danger' : 'primary'" @click="feedback.resolveConfirm(true)">
          {{ feedback.confirmRequest.confirmLabel }}
        </BaseButton>
      </div>
    </section>

    <section v-else-if="feedback.promptRequest" class="admin-dialog" role="dialog" aria-modal="true" :aria-labelledby="`prompt-title-${feedback.promptRequest.id}`">
      <p class="admin-eyebrow">interaction // reply</p>
      <h2 :id="`prompt-title-${feedback.promptRequest.id}`" class="admin-dialog__title">{{ feedback.promptRequest.title }}</h2>
      <label class="admin-dialog__label">
        <span>{{ feedback.promptRequest.label }}</span>
        <textarea ref="promptInput" v-model="promptValue" rows="5" autofocus></textarea>
      </label>
      <div class="admin-dialog__actions">
        <BaseButton variant="secondary" @click="feedback.resolvePrompt(null)">取消</BaseButton>
        <BaseButton :disabled="!promptValue.trim()" @click="submitPrompt">{{ feedback.promptRequest.confirmLabel }}</BaseButton>
      </div>
    </section>
  </div>
</template>
