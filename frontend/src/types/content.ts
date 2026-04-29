export interface TagOption {
  id: string;
  name: string;
  slug: string;
  color?: string | null;
}

export interface PublicCategory {
  id: string;
  name: string;
  slug: string;
  description?: string | null;
  articleCount: number;
  sortOrder?: number | null;
}

export interface AdminCategory extends PublicCategory {
  bizType: string;
  status: 'ENABLED' | 'DISABLED';
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface PublicTag {
  id: string;
  name: string;
  slug: string;
  color?: string | null;
  articleCount: number;
}

export interface AdminTag extends PublicTag {
  description?: string | null;
  status: 'ENABLED' | 'DISABLED';
  createdAt?: string | null;
  updatedAt?: string | null;
}

export type ArticleStatus = 'DRAFT' | 'PUBLISHED' | 'HIDDEN';

export interface ArticleListItem {
  id: string;
  title: string;
  slug: string;
  summary?: string | null;
  coverImage?: string | null;
  categoryId?: string | null;
  categoryName?: string | null;
  tags: TagOption[];
  viewCount: number;
  likeCount: number;
  commentCount: number;
  readingTime: number;
  isTop: boolean;
  publishedAt?: string | null;
  updatedAt?: string | null;
}

export interface ArticleDetail extends ArticleListItem {
  content: string;
}

export interface AdminArticleListItem extends ArticleListItem {
  status: ArticleStatus;
  createdAt?: string | null;
}

export interface AdminArticleDetail extends AdminArticleListItem {
  content: string;
}

export interface ArticleQuery {
  keyword?: string;
  categoryId?: string;
  tagId?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface AdminArticleQuery {
  keyword?: string;
  categoryId?: string;
  status?: ArticleStatus | '';
  page?: number;
  size?: number;
}

export interface ArticleSavePayload {
  title: string;
  slug: string;
  summary?: string;
  content: string;
  coverImage?: string;
  categoryId: string;
  tagIds: string[];
  status: ArticleStatus;
  isTop: boolean;
  readingTime?: number;
}

export interface CategorySavePayload {
  bizType?: 'ARTICLE' | 'NOTE';
  name: string;
  slug: string;
  description?: string;
  sortOrder?: number;
  status?: 'ENABLED' | 'DISABLED';
}

export interface TagSavePayload {
  name: string;
  slug: string;
  color?: string;
  description?: string;
  status?: 'ENABLED' | 'DISABLED';
}
