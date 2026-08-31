export type FileBizType = 'article-cover' | 'project-cover' | 'avatar' | 'site' | 'other';

export interface FileUploadResult {
  url: string;
  filename: string;
  originalFilename: string;
  contentType: string;
  size: number;
}
