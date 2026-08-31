import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type { AdminProjectQuery, ProjectDetail, ProjectItem, ProjectSavePayload } from '@/types/project';

export async function fetchAdminProjects(params: AdminProjectQuery = {}): Promise<PageResult<ProjectItem>> {
  const response = await http.get<Result<PageResult<ProjectItem>>>('/admin/projects', { params });
  return response.data.data;
}

export async function fetchAdminProject(id: string): Promise<ProjectDetail> {
  const response = await http.get<Result<ProjectDetail>>(`/admin/projects/${id}`);
  return response.data.data;
}

export async function createAdminProject(payload: ProjectSavePayload): Promise<ProjectDetail> {
  const response = await http.post<Result<ProjectDetail>>('/admin/projects', payload);
  return response.data.data;
}

export async function updateAdminProject(id: string, payload: ProjectSavePayload): Promise<ProjectDetail> {
  const response = await http.put<Result<ProjectDetail>>(`/admin/projects/${id}`, payload);
  return response.data.data;
}

export async function deleteAdminProject(id: string): Promise<void> {
  await http.delete<Result<void>>(`/admin/projects/${id}`);
}
