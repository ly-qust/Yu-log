const PLACEHOLDER_MARKERS = [
  'your-name',
  'github.com/xxx',
  'example.com',
  'your-domain',
  'localhost',
];

export function safeExternalUrl(value?: string | null): string {
  if (!value || PLACEHOLDER_MARKERS.some((marker) => value.toLowerCase().includes(marker))) {
    return '';
  }

  try {
    const url = new URL(value.trim());
    return url.protocol === 'https:' || url.protocol === 'http:' ? url.toString() : '';
  } catch {
    return '';
  }
}
