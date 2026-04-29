import { http } from './http';
import type { Result } from '@/types/api';
import type { AuthTokenResponse, LoginPayload, UserInfo } from '@/types/auth';

export async function loginApi(payload: LoginPayload): Promise<AuthTokenResponse> {
  const response = await http.post<Result<AuthTokenResponse>>('/auth/login', payload);
  return response.data.data;
}

export async function refreshApi(refreshToken: string): Promise<AuthTokenResponse> {
  const response = await http.post<Result<AuthTokenResponse>>('/auth/refresh', { refreshToken });
  return response.data.data;
}

export async function logoutApi(): Promise<void> {
  await http.post<Result<void>>('/auth/logout');
}

export async function fetchMeApi(): Promise<UserInfo> {
  const response = await http.get<Result<UserInfo>>('/users/me');
  return response.data.data;
}
