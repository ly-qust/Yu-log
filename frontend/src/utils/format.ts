export function formatRouteLabel(value: string): string {
  return value.trim();
}

export function formatDateTime(value?: string | null): string {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

export function formatDate(value?: string | null): string {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(date);
}

export function formatCount(value?: number | null): string {
  return new Intl.NumberFormat('zh-CN').format(value ?? 0);
}

export function formatArticleStatus(value?: string | null): string {
  const statusMap: Record<string, string> = {
    PUBLISHED: '已发布',
    DRAFT: '草稿',
    HIDDEN: '已隐藏',
  };

  return value ? statusMap[value] ?? value : '-';
}

export function formatInteractionStatus(value?: string | null): string {
  const statusMap: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
  };

  return value ? statusMap[value] ?? value : '-';
}

export function formatProjectStatus(value?: string | null): string {
  const statusMap: Record<string, string> = {
    PLANNING: '规划中',
    DEVELOPING: '开发中',
    COMPLETED: '已完成',
    PUBLISHED: '已展示',
  };

  return value ? statusMap[value] ?? value : '-';
}

export function formatBoolean(value?: boolean | null): string {
  return value ? '是' : '否';
}
