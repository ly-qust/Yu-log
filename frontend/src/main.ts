import { createPinia } from 'pinia';
import { createApp } from 'vue';

import App from './App.vue';
import router from './router';
import { useThemeStore } from './stores/theme';
import './assets/styles/main.css';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
useThemeStore(pinia).initialize();
app.use(router);
app.mount('#app');
