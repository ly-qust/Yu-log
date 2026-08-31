import { http } from './http';
import type { Result } from '@/types/api';

export interface AdminDashboardStats {
  articleCount: number;
  messageCount: number;
  projectCount: number;
  noteCount: number;
}

export async function fetchAdminDashboard(): Promise<AdminDashboardStats> {
  const response = await http.get<Result<AdminDashboardStats>>('/admin/dashboard');
  return response.data.data;
}
