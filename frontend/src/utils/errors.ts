import axios from 'axios';

interface ApiErrorBody {
  message?: string;
}

export function getErrorMessage(error: unknown, fallback = 'Request failed'): string {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    return error.response?.data?.message || error.message || fallback;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return fallback;
}
