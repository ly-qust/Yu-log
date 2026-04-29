import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type {
  AdminArticleDetail,
  AdminArticleListItem,
  AdminArticleQuery,
  ArticleSavePayload,
  ArticleStatus,
} from '@/types/content';

export async function fetchAdminArticles(params: AdminArticleQuery = {}): Promise<PageResult<AdminArticleListItem>> {
  const response = await http.get<Result<PageResult<AdminArticleListItem>>>('/admin/articles', { params });
  return response.data.data;
}

export async function fetchAdminArticle(id: string): Promise<AdminArticleDetail> {
  const response = await http.get<Result<AdminArticleDetail>>(`/admin/articles/${id}`);
  return response.data.data;
}

export async function createAdminArticle(payload: ArticleSavePayload): Promise<AdminArticleDetail> {
  const response = await http.post<Result<AdminArticleDetail>>('/admin/articles', payload);
  return response.data.data;
}

export async function updateAdminArticle(id: string, payload: ArticleSavePayload): Promise<AdminArticleDetail> {
  const response = await http.put<Result<AdminArticleDetail>>(`/admin/articles/${id}`, payload);
  return response.data.data;
}

export async function deleteAdminArticle(id: string): Promise<void> {
  await http.delete<Result<void>>(`/admin/articles/${id}`);
}

export async function updateAdminArticleStatus(id: string, status: ArticleStatus): Promise<AdminArticleDetail> {
  const response = await http.put<Result<AdminArticleDetail>>(`/admin/articles/${id}/status`, { status });
  return response.data.data;
}

export async function updateAdminArticleTop(id: string, isTop: boolean): Promise<AdminArticleDetail> {
  const response = await http.put<Result<AdminArticleDetail>>(`/admin/articles/${id}/top`, { isTop });
  return response.data.data;
}
