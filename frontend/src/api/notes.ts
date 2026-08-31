import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type { NoteItem, NoteQuery } from '@/types/note';

export async function fetchNotes(params: NoteQuery = {}): Promise<PageResult<NoteItem>> {
  const response = await http.get<Result<PageResult<NoteItem>>>('/notes', { params });
  return response.data.data;
}

export async function fetchNoteDetail(id: string): Promise<NoteItem> {
  const response = await http.get<Result<NoteItem>>(`/notes/${id}`);
  return response.data.data;
}
