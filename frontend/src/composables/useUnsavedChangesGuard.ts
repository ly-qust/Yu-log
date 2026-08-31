import { computed, onMounted, onUnmounted, ref } from 'vue';
import { onBeforeRouteLeave } from 'vue-router';

import { useAdminFeedbackStore } from '@/stores/adminFeedback';

export function useUnsavedChangesGuard(getSnapshot: () => string, options: { label: string; isSaving?: () => boolean }) {
  const feedback = useAdminFeedbackStore();
  const ready = ref(false);
  const cleanSnapshot = ref('');
  const isDirty = computed(() => ready.value && cleanSnapshot.value !== getSnapshot());

  function markClean(snapshot = getSnapshot()) {
    cleanSnapshot.value = snapshot;
    ready.value = true;
  }

  onBeforeRouteLeave(async () => {
    if (!isDirty.value || options.isSaving?.()) {
      return true;
    }
    return feedback.confirm({
      title: '放弃未保存修改？',
      message: `${options.label}还有未保存修改，离开后这些内容可能丢失。`,
      confirmLabel: '离开页面',
      danger: true,
    });
  });

  function handleBeforeUnload(event: BeforeUnloadEvent) {
    if (!isDirty.value || options.isSaving?.()) return;
    event.preventDefault();
    event.returnValue = '';
  }

  onMounted(() => window.addEventListener('beforeunload', handleBeforeUnload));
  onUnmounted(() => window.removeEventListener('beforeunload', handleBeforeUnload));

  return { isDirty, markClean };
}
