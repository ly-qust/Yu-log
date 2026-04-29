import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type { AdminComment, AdminCommentQuery, InteractionStatus } from '@/types/interaction';

export async function fetchAdminComments(params: AdminCommentQuery = {}): Promise<PageResult<AdminComment>> {
  const response = await http.get<Result<PageResult<AdminComment>>>('/admin/comments', { params });
  return response.data.data;
}

export async function updateAdminCommentStatus(id: string, status: InteractionStatus): Promise<AdminComment> {
  const response = await http.put<Result<AdminComment>>(`/admin/comments/${id}/status`, { status });
  return response.data.data;
}

export async function replyAdminComment(id: string, adminReply: string): Promise<AdminComment> {
  const response = await http.put<Result<AdminComment>>(`/admin/comments/${id}/reply`, { adminReply });
  return response.data.data;
}

export async function deleteAdminComment(id: string): Promise<void> {
  await http.delete<Result<void>>(`/admin/comments/${id}`);
}
