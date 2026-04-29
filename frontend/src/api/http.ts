import axios from 'axios';

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 10000,
});

http.interceptors.request.use((config) => {
  const token = window.localStorage.getItem('yu_log_access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.localStorage.removeItem('yu_log_access_token');
      window.localStorage.removeItem('yu_log_refresh_token');
      window.localStorage.removeItem('yu_log_user');
    }
    return Promise.reject(error);
  },
);
