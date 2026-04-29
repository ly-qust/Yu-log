import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type { AdminMessage, AdminMessageQuery, InteractionStatus } from '@/types/interaction';

export async function fetchAdminMessages(params: AdminMessageQuery = {}): Promise<PageResult<AdminMessage>> {
  const response = await http.get<Result<PageResult<AdminMessage>>>('/admin/messages', { params });
  return response.data.data;
}

export async function updateAdminMessageStatus(id: string, status: InteractionStatus): Promise<AdminMessage> {
  const response = await http.put<Result<AdminMessage>>(`/admin/messages/${id}/status`, { status });
  return response.data.data;
}

export async function replyAdminMessage(id: string, adminReply: string): Promise<AdminMessage> {
  const response = await http.put<Result<AdminMessage>>(`/admin/messages/${id}/reply`, { adminReply });
  return response.data.data;
}

export async function deleteAdminMessage(id: string): Promise<void> {
  await http.delete<Result<void>>(`/admin/messages/${id}`);
}
