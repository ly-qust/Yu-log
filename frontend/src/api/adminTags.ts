import { http } from './http';
import type { Result } from '@/types/api';
import type { AdminTag, TagSavePayload } from '@/types/content';

export async function fetchAdminTags(): Promise<AdminTag[]> {
  const response = await http.get<Result<AdminTag[]>>('/admin/tags');
  return response.data.data;
}

export async function createAdminTag(payload: TagSavePayload): Promise<AdminTag> {
  const response = await http.post<Result<AdminTag>>('/admin/tags', payload);
  return response.data.data;
}

export async function updateAdminTag(id: string, payload: TagSavePayload): Promise<AdminTag> {
  const response = await http.put<Result<AdminTag>>(`/admin/tags/${id}`, payload);
  return response.data.data;
}

export async function deleteAdminTag(id: string): Promise<void> {
  await http.delete<Result<void>>(`/admin/tags/${id}`);
}
