export interface TechGroupDefinition {
  key: string;
  label: string;
  hint: string;
  patterns: RegExp[];
}

export const homepageIdentity = {
  eyebrowFallback: '计算机科学 · 后端开发 · 数字花园',
  heroTitleFallback: '你好，我是 Yu。',
  heroDescriptionFallback: '我构建后端系统，探索 AI 应用，也把学习过程沉淀成一座持续生长的数字花园。',
  statusFallback: '在线学习中',
  closingLines: ['Still learning.', 'Still building.', 'Still growing.'],
} as const;

export const techGroupDefinitions: TechGroupDefinition[] = [
  { key: 'backend', label: '后端系统', hint: 'services / architecture', patterns: [/java/i, /spring/i, /mybatis/i, /maven/i, /gradle/i, /backend/i] },
  { key: 'data', label: '数据与存储', hint: 'storage / cache', patterns: [/mysql/i, /redis/i, /postgres/i, /database/i, /sql/i] },
  { key: 'messaging', label: '消息与事件', hint: 'async / events', patterns: [/rabbitmq/i, /kafka/i, /rocketmq/i, /message/i, /\bmq\b/i] },
  { key: 'infrastructure', label: '基础设施', hint: 'runtime / delivery', patterns: [/linux/i, /docker/i, /nginx/i, /git/i, /devops/i, /kubernetes/i, /k8s/i] },
  { key: 'frontend', label: '前端体验', hint: 'interface / experience', patterns: [/vue/i, /typescript/i, /javascript/i, /tailwind/i, /vite/i, /frontend/i] },
  { key: 'ai', label: 'AI 应用', hint: 'models / applications', patterns: [/\bai\b/i, /rag/i, /llm/i, /openai/i, /人工智能/i, /大模型/i] },
];
