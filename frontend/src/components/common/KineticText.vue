<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ text: string }>();

const phrases = computed(() => props.text.split(/(?<=[，。！？；])/u).map((phrase) => phrase.trim()).filter(Boolean));
</script>

<template>
  <span class="kinetic-text">
    <span class="sr-only">{{ text }}</span>
    <span aria-hidden="true" class="kinetic-text__visual">
      <span v-for="(phrase, index) in phrases" :key="`${phrase}-${index}`" class="kinetic-text__phrase" :style="{ '--kinetic-index': index }">{{ phrase }}</span>
    </span>
  </span>
</template>

<style scoped>
.kinetic-text__visual { display: inline; }
.kinetic-text__phrase { display: inline-block; opacity: 0; filter: blur(5px); transform: translateY(.35em); animation: kinetic-phrase-in 680ms var(--ease-emphasized) calc(var(--kinetic-index) * 90ms + 80ms) forwards; }
@keyframes kinetic-phrase-in { to { opacity: 1; filter: blur(0); transform: translateY(0); } }
@media (hover: hover) and (pointer: fine) {
  .kinetic-text__phrase { transition: letter-spacing 220ms var(--ease-emphasized), transform 220ms var(--ease-emphasized), font-weight 220ms var(--ease-emphasized); }
  .kinetic-text__phrase:hover { font-weight: 650; letter-spacing: .01em; transform: translateY(-1px); }
}
@media (prefers-reduced-motion: reduce) {
  .kinetic-text__phrase { opacity: 1; filter: none; transform: none; animation: none; transition: none; }
}
</style>
