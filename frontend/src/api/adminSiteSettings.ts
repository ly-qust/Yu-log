import { http } from './http';
import type { Result } from '@/types/api';
import type { SiteSetting, SiteSettingBatchItem, SiteSettingUpdatePayload } from '@/types/site';

export async function fetchAdminSiteSettings(group?: string): Promise<SiteSetting[]> {
  const response = await http.get<Result<SiteSetting[]>>('/admin/site-settings', {
    params: group ? { group } : undefined,
  });
  return response.data.data;
}

export async function updateAdminSiteSetting(key: string, payload: SiteSettingUpdatePayload): Promise<SiteSetting> {
  const response = await http.put<Result<SiteSetting>>(`/admin/site-settings/${encodeURIComponent(key)}`, payload);
  return response.data.data;
}

export async function updateAdminSiteSettingsBatch(payload: SiteSettingBatchItem[]): Promise<SiteSetting[]> {
  const response = await http.put<Result<SiteSetting[]>>('/admin/site-settings/batch', payload);
  return response.data.data;
}
