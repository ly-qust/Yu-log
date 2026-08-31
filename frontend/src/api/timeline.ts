import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type { TimelineEventItem, TimelineQuery } from '@/types/timeline';

export async function fetchTimeline(params: TimelineQuery = {}): Promise<PageResult<TimelineEventItem>> {
  const response = await http.get<Result<PageResult<TimelineEventItem>>>('/timeline', { params });
  return response.data.data;
}
