import axios from 'axios';

interface ApiErrorBody {
  message?: string;
}

export function getErrorMessage(error: unknown, fallback = '加载失败，请稍后重试'): string {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    return error.response?.data?.message || error.message || fallback;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return fallback;
}
