import { http } from './http';
import type { Result } from '@/types/api';
import type { PublicTag } from '@/types/content';

export async function fetchTags(): Promise<PublicTag[]> {
  const response = await http.get<Result<PublicTag[]>>('/tags');
  return response.data.data;
}
