import { http } from './http';
import type { Result } from '@/types/api';
import type { AboutData } from '@/types/site';

export async function fetchAbout(): Promise<AboutData> {
  const response = await http.get<Result<AboutData>>('/about');
  return response.data.data;
}
