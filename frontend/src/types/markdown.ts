export interface ArticleHeading {
  id: string;
  text: string;
  level: number;
}

export interface RenderedMarkdown {
  html: string;
  headings: ArticleHeading[];
}
