<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ skills: string[] }>();

type SkillGroup = {
  label: string;
  match: readonly string[];
  tone: 'brand' | 'accent' | 'neutral';
  items: string[];
};

const groupDefinitions = [
  { label: '后端工程', match: ['java', 'spring'], tone: 'brand' },
  { label: '数据与缓存', match: ['mysql', 'redis'], tone: 'accent' },
  { label: '基础设施', match: ['linux', 'docker', 'nginx'], tone: 'neutral' },
  { label: '前端体验', match: ['vue', 'typescript', 'vite'], tone: 'brand' },
  { label: 'AI 应用', match: ['ai', 'llm', 'rag', 'prompt'], tone: 'accent' },
] as const;

const groups = computed<SkillGroup[]>(() => {
  const used = new Set<string>();
  const result: SkillGroup[] = groupDefinitions.map((definition): SkillGroup => {
    const items = props.skills.filter((skill) => {
      const normalized = skill.toLowerCase();
      const matched = definition.match.some((token) => normalized.includes(token));
      if (matched) used.add(skill);
      return matched;
    });
    return { ...definition, items };
  }).filter((group) => group.items.length);
  const other = props.skills.filter((skill) => !used.has(skill));
  if (other.length) result.push({ label: '其他工具', match: [], tone: 'neutral', items: other });
  return result;
});
</script>

<template>
  <section class="about-skills" aria-labelledby="about-skills-title">
    <div class="about-section-heading">
      <p class="about-kicker">技术方向 // ENGINEERING</p>
      <h2 id="about-skills-title">正在认真学会使用的工具。</h2>
      <p>技能按领域归组，只展示当前 About 接口返回的公开技术，不使用主观熟练度。</p>
    </div>
    <div v-if="groups.length" class="about-skill-grid">
      <div v-for="group in groups" :key="group.label" class="about-skill-group" :class="`is-${group.tone}`">
        <div class="about-skill-group__head"><span>{{ group.label }}</span><span>{{ String(group.items.length).padStart(2, '0') }}</span></div>
        <div class="about-skill-group__items"><span v-for="skill in group.items" :key="skill">{{ skill }}</span></div>
      </div>
    </div>
    <p v-else class="about-muted">暂无技能配置。</p>
  </section>
</template>

<style scoped>
.about-section-heading h2 { max-width: 38rem; margin-top: .65rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(1.65rem, 4vw, 2.5rem); font-weight: 650; line-height: 1.12; letter-spacing: -.04em; }
.about-section-heading > p:last-child { max-width: 37rem; margin-top: .9rem; font-size: .88rem; line-height: 1.8; color: rgb(var(--color-text-secondary)); }
.about-skill-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1px; margin-top: 2rem; overflow: hidden; border: 1px solid rgb(var(--color-border-subtle) / .68); border-radius: .8rem; background: rgb(var(--color-border-subtle) / .68); }
.about-skill-group { position: relative; min-height: 8.2rem; padding: 1.15rem; background: rgb(var(--color-surface-elevated) / .62); }
.about-skill-group::before { position: absolute; left: 0; top: 1rem; bottom: 1rem; width: 2px; background: rgb(var(--color-text-muted) / .35); content: ''; }
.about-skill-group.is-brand::before { background: rgb(var(--color-brand-primary)); }
.about-skill-group.is-accent::before { background: rgb(var(--color-accent-secondary)); }
.about-skill-group__head { display: flex; justify-content: space-between; gap: 1rem; font-family: 'JetBrains Mono', monospace; font-size: .6rem; text-transform: uppercase; letter-spacing: .08em; color: rgb(var(--color-text-muted)); }
.about-skill-group__head span:last-child { color: rgb(var(--color-brand-primary)); }
.about-skill-group__items { display: flex; flex-wrap: wrap; gap: .45rem .8rem; margin-top: 1.25rem; }
.about-skill-group__items span { font-size: .86rem; color: rgb(var(--color-text-primary)); }
.about-muted { margin-top: 1.5rem; font-size: .85rem; color: rgb(var(--color-text-muted)); }
@media (max-width: 639px) { .about-skill-grid { grid-template-columns: 1fr; } }
</style>
