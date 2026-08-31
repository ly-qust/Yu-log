export type ProjectStatus = 'PLANNING' | 'DEVELOPING' | 'COMPLETED';

export interface ProjectItem {
  id: string;
  name: string;
  slug: string;
  description?: string | null;
  coverImage?: string | null;
  techStack: string[];
  status: ProjectStatus | string;
  githubUrl?: string | null;
  demoUrl?: string | null;
  sortOrder?: number | null;
  visible?: boolean | null;
  featured?: boolean | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface ProjectDetail extends ProjectItem {
  detailContent?: string | null;
}

export interface ProjectArchitectureNode {
  id: string;
  label: string;
  caption?: string;
  tone?: 'brand' | 'accent' | 'neutral';
}

export interface ProjectGalleryImage {
  src: string;
  alt: string;
  caption?: string;
}

export interface ProjectQuery {
  keyword?: string;
  techStack?: string;
  status?: string;
  page?: number;
  size?: number;
}

export interface AdminProjectQuery {
  keyword?: string;
  status?: string;
  page?: number;
  size?: number;
}

export interface ProjectSavePayload {
  name: string;
  slug: string;
  description?: string;
  detailContent?: string;
  coverImage?: string;
  techStack: string[];
  status: ProjectStatus;
  githubUrl?: string;
  demoUrl?: string;
  sortOrder?: number;
  visible: boolean;
}
