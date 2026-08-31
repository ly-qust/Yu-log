import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type { AdminTimelineQuery, TimelineEventItem, TimelineSavePayload } from '@/types/timeline';

export async function fetchAdminTimeline(params: AdminTimelineQuery = {}): Promise<PageResult<TimelineEventItem>> {
  const response = await http.get<Result<PageResult<TimelineEventItem>>>('/admin/timeline', { params });
  return response.data.data;
}

export async function fetchAdminTimelineEvent(id: string): Promise<TimelineEventItem> {
  const response = await http.get<Result<TimelineEventItem>>(`/admin/timeline/${id}`);
  return response.data.data;
}

export async function createAdminTimelineEvent(payload: TimelineSavePayload): Promise<TimelineEventItem> {
  const response = await http.post<Result<TimelineEventItem>>('/admin/timeline', payload);
  return response.data.data;
}

export async function updateAdminTimelineEvent(id: string, payload: TimelineSavePayload): Promise<TimelineEventItem> {
  const response = await http.put<Result<TimelineEventItem>>(`/admin/timeline/${id}`, payload);
  return response.data.data;
}

export async function deleteAdminTimelineEvent(id: string): Promise<void> {
  await http.delete<Result<void>>(`/admin/timeline/${id}`);
}
