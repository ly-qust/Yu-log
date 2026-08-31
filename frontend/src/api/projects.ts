import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type { ProjectDetail, ProjectItem, ProjectQuery } from '@/types/project';

export async function fetchProjects(params: ProjectQuery = {}): Promise<PageResult<ProjectItem>> {
  const response = await http.get<Result<PageResult<ProjectItem>>>('/projects', { params });
  return response.data.data;
}

export async function fetchProjectDetail(id: string): Promise<ProjectDetail> {
  const response = await http.get<Result<ProjectDetail>>(`/projects/${id}`);
  return response.data.data;
}
