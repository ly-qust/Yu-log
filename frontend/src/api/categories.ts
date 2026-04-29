import { http } from './http';
import type { Result } from '@/types/api';
import type { PublicCategory } from '@/types/content';

export async function fetchCategories(): Promise<PublicCategory[]> {
  const response = await http.get<Result<PublicCategory[]>>('/categories');
  return response.data.data;
}
