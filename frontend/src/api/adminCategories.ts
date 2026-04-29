import { http } from './http';
import type { Result } from '@/types/api';
import type { AdminCategory, CategorySavePayload } from '@/types/content';

export async function fetchAdminCategories(): Promise<AdminCategory[]> {
  const response = await http.get<Result<AdminCategory[]>>('/admin/categories');
  return response.data.data;
}

export async function createAdminCategory(payload: CategorySavePayload): Promise<AdminCategory> {
  const response = await http.post<Result<AdminCategory>>('/admin/categories', payload);
  return response.data.data;
}

export async function updateAdminCategory(id: string, payload: CategorySavePayload): Promise<AdminCategory> {
  const response = await http.put<Result<AdminCategory>>(`/admin/categories/${id}`, payload);
  return response.data.data;
}

export async function deleteAdminCategory(id: string): Promise<void> {
  await http.delete<Result<void>>(`/admin/categories/${id}`);
}
