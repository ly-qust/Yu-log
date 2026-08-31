import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type { AdminNoteQuery, NoteItem, NoteSavePayload } from '@/types/note';

export async function fetchAdminNotes(params: AdminNoteQuery = {}): Promise<PageResult<NoteItem>> {
  const response = await http.get<Result<PageResult<NoteItem>>>('/admin/notes', { params });
  return response.data.data;
}

export async function fetchAdminNote(id: string): Promise<NoteItem> {
  const response = await http.get<Result<NoteItem>>(`/admin/notes/${id}`);
  return response.data.data;
}

export async function createAdminNote(payload: NoteSavePayload): Promise<NoteItem> {
  const response = await http.post<Result<NoteItem>>('/admin/notes', payload);
  return response.data.data;
}

export async function updateAdminNote(id: string, payload: NoteSavePayload): Promise<NoteItem> {
  const response = await http.put<Result<NoteItem>>(`/admin/notes/${id}`, payload);
  return response.data.data;
}

export async function deleteAdminNote(id: string): Promise<void> {
  await http.delete<Result<void>>(`/admin/notes/${id}`);
}
