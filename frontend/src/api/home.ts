import { http } from './http';
import type { Result } from '@/types/api';
import type { HomeOverview } from '@/types/site';

export async function fetchHomeOverview(): Promise<HomeOverview> {
  const response = await http.get<Result<HomeOverview>>('/home/overview');
  return response.data.data;
}
