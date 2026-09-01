(() => {
  try {
    const saved = localStorage.getItem('yu-log-theme');
    const preference = ['light', 'dark', 'system'].includes(saved) ? saved : 'system';
    const theme = preference === 'system'
      ? (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
      : preference;
    document.documentElement.dataset.theme = theme;
    document.documentElement.dataset.themePreference = preference;
    document.documentElement.style.colorScheme = theme;
  } catch (_) {
    document.documentElement.dataset.theme = 'dark';
  }
})();
