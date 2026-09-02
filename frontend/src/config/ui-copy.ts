/**
 * 高频共享界面文案。内容本身仍然来自 API，这里只收口重复出现的 UI 语言。
 */
export const uiCopy = {
  search: '搜索',
  searchPlaceholder: '搜索文章、项目和笔记……',
  loading: '正在加载',
  retry: '重新加载',
  close: '关闭',
  cancel: '取消',
  back: '返回',
  previous: '上一篇',
  next: '下一篇',
  copy: '复制',
  copied: '已复制',
  copyFailed: '复制失败',
  open: '打开',
  noResults: '没有找到相关内容',
  tryAnother: '换个关键词试试？',
  navigate: '选择',
  select: '选择',
  enter: '打开',
  escape: '关闭',
  theme: {
    dark: '深色模式',
    light: '浅色模式',
    system: '跟随系统',
  },
  groups: {
    navigate: '前往',
    theme: '主题',
    articles: '文章',
    projects: '项目',
    notes: '笔记',
  },
} as const;

export const publicSectionCopy = {
  articles: { title: '文章', eyebrow: 'LATEST WRITING' },
  projects: { title: '精选项目', eyebrow: 'SELECTED WORK' },
  notes: { title: '随手记', eyebrow: 'DIGITAL GARDEN' },
  timeline: { title: '成长轨迹', eyebrow: 'GROWTH LOG' },
  about: { title: '关于我', eyebrow: 'ABOUT' },
} as const;
