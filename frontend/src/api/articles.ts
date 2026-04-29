import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type { ArticleDetail, ArticleListItem, ArticleQuery } from '@/types/content';

export async function fetchArticles(params: ArticleQuery = {}): Promise<PageResult<ArticleListItem>> {
  const response = await http.get<Result<PageResult<ArticleListItem>>>('/articles', { params });
  return response.data.data;
}

export async function fetchArticleDetail(id: string): Promise<ArticleDetail> {
  const response = await http.get<Result<ArticleDetail>>(`/articles/${id}`);
  return response.data.data;
}

export async function likeArticle(id: string): Promise<number> {
  const response = await http.post<Result<{ likeCount: number }>>(`/articles/${id}/like`);
  return response.data.data.likeCount;
}
