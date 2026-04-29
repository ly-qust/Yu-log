import { http } from './http';
import type { Result } from '@/types/api';
import type { CommentSubmitPayload, PublicComment } from '@/types/interaction';

export async function fetchArticleComments(articleId: string): Promise<PublicComment[]> {
  const response = await http.get<Result<PublicComment[]>>(`/articles/${articleId}/comments`);
  return response.data.data;
}

export async function submitArticleComment(articleId: string, payload: CommentSubmitPayload): Promise<string> {
  const response = await http.post<Result<string>>(`/articles/${articleId}/comments`, payload);
  return response.data.data;
}
