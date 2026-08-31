import { http } from './http';
import type { Result } from '@/types/api';
import type { FileBizType, FileUploadResult } from '@/types/file';

export async function uploadFile(file: File, bizType: FileBizType = 'other'): Promise<FileUploadResult> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('bizType', bizType);

  const response = await http.post<Result<FileUploadResult>>('/admin/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data.data;
}
