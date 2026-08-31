export interface TechGroupDefinition {
  key: string;
  label: string;
  hint: string;
  patterns: RegExp[];
}

export const homepageIdentity = {
  eyebrowFallback: 'CS Student · Backend Developer · Digital Gardener',
  heroTitleFallback: 'Hi, I’m Yu.',
  heroDescriptionFallback: '我构建后端系统，探索 AI 应用，也把学习过程沉淀成一座持续生长的数字花园。',
  statusFallback: 'Online & Learning',
  closingLines: ['Still learning.', 'Still building.', 'Still growing.'],
} as const;

export const techGroupDefinitions: TechGroupDefinition[] = [
  { key: 'backend', label: 'Backend', hint: 'services / architecture', patterns: [/java/i, /spring/i, /mybatis/i, /maven/i, /gradle/i, /backend/i] },
  { key: 'data', label: 'Data', hint: 'storage / cache', patterns: [/mysql/i, /redis/i, /postgres/i, /database/i, /sql/i] },
  { key: 'messaging', label: 'Messaging', hint: 'async / events', patterns: [/rabbitmq/i, /kafka/i, /rocketmq/i, /message/i, /\bmq\b/i] },
  { key: 'infrastructure', label: 'Infrastructure', hint: 'runtime / delivery', patterns: [/linux/i, /docker/i, /nginx/i, /git/i, /devops/i, /kubernetes/i, /k8s/i] },
  { key: 'frontend', label: 'Frontend', hint: 'interface / experience', patterns: [/vue/i, /typescript/i, /javascript/i, /tailwind/i, /vite/i, /frontend/i] },
  { key: 'ai', label: 'AI', hint: 'models / applications', patterns: [/\bai\b/i, /rag/i, /llm/i, /openai/i, /人工智能/i, /大模型/i] },
];
