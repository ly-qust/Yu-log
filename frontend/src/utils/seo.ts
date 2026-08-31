export interface SeoOptions {
  title: string;
  description: string;
  canonicalPath: string;
  type?: 'website' | 'article';
  image?: string;
  publishedTime?: string | null;
  modifiedTime?: string | null;
  author?: string;
  structuredData?: Record<string, unknown>;
}

const SEO_MARKER = 'data-yulog-seo';

function absoluteUrl(value: string): string {
  try {
    return new URL(value, window.location.origin).toString();
  } catch {
    return window.location.href;
  }
}

function appendMeta(attribute: 'name' | 'property', key: string, content: string) {
  const element = document.createElement('meta');
  element.setAttribute(attribute, key);
  element.content = content;
  element.setAttribute(SEO_MARKER, 'true');
  document.head.appendChild(element);
}

export function applySeo(options: SeoOptions): () => void {
  const previousTitle = document.title;
  const description = document.querySelector<HTMLMetaElement>('meta[name="description"]');
  const previousDescription = description?.content;
  document.head.querySelectorAll(`[${SEO_MARKER}]`).forEach((element) => element.remove());
  const canonicalUrl = absoluteUrl(options.canonicalPath);
  document.title = options.title;

  if (description) description.content = options.description;

  const canonical = document.createElement('link');
  canonical.rel = 'canonical';
  canonical.href = canonicalUrl;
  canonical.setAttribute(SEO_MARKER, 'true');
  document.head.appendChild(canonical);

  appendMeta('property', 'og:title', options.title);
  appendMeta('property', 'og:description', options.description);
  appendMeta('property', 'og:type', options.type || 'website');
  appendMeta('property', 'og:url', canonicalUrl);
  appendMeta('name', 'twitter:card', options.image ? 'summary_large_image' : 'summary');
  appendMeta('name', 'twitter:title', options.title);
  appendMeta('name', 'twitter:description', options.description);
  if (options.image) {
    const imageUrl = absoluteUrl(options.image);
    appendMeta('property', 'og:image', imageUrl);
    appendMeta('name', 'twitter:image', imageUrl);
  }
  if (options.publishedTime) appendMeta('property', 'article:published_time', options.publishedTime);
  if (options.modifiedTime) appendMeta('property', 'article:modified_time', options.modifiedTime);
  if (options.author) appendMeta('name', 'author', options.author);

  if (options.structuredData) {
    const script = document.createElement('script');
    script.type = 'application/ld+json';
    script.textContent = JSON.stringify(options.structuredData).replace(/</g, '\\u003c');
    script.setAttribute(SEO_MARKER, 'true');
    document.head.appendChild(script);
  }

  return () => {
    document.head.querySelectorAll(`[${SEO_MARKER}]`).forEach((element) => element.remove());
    document.title = previousTitle;
    if (description && previousDescription !== undefined) description.content = previousDescription;
  };
}
