import { http } from './http';
import type { PageResult, Result } from '@/types/api';
import type { MessageSubmitPayload, PublicMessage } from '@/types/interaction';

export async function fetchMessages(params: { page?: number; size?: number } = {}): Promise<PageResult<PublicMessage>> {
  const response = await http.get<Result<PageResult<PublicMessage>>>('/messages', { params });
  return response.data.data;
}

export async function submitMessage(payload: MessageSubmitPayload): Promise<string> {
  const response = await http.post<Result<string>>('/messages', payload);
  return response.data.data;
}
