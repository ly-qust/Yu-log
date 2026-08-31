export interface LocalDraftRecord<T> {
  savedAt: number;
  data: T;
}

function storageKey(key: string) {
  return `yu-log-admin-draft:${key}`;
}

export function readLocalDraft<T>(key: string): LocalDraftRecord<T> | null {
  const raw = window.localStorage.getItem(storageKey(key));
  if (!raw) return null;
  try {
    return JSON.parse(raw) as LocalDraftRecord<T>;
  } catch {
    window.localStorage.removeItem(storageKey(key));
    return null;
  }
}

export function writeLocalDraft<T>(key: string, data: T): LocalDraftRecord<T> {
  const record = { savedAt: Date.now(), data };
  window.localStorage.setItem(storageKey(key), JSON.stringify(record));
  return record;
}

export function discardLocalDraft(key: string) {
  window.localStorage.removeItem(storageKey(key));
}

export function formatLocalDraftTime(value: number) {
  return new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit' }).format(value);
}
