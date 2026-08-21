/**
 * 模块1、4、5：前端统一 HTTP、Access Token 注入与单飞刷新。
 * 功能：解析字符串响应码、在内存保存令牌，并在并发 401 时只发起一次刷新请求。
 * 技术栈：TypeScript、Axios 请求/响应拦截器与 HttpOnly Cookie。
 */
import axios, { type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios';
import type { TokenResponse } from '@/types/auth';

export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  requestId?: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
}

export const http = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  withCredentials: true,
});

let accessToken: string | null = null;
let refreshPromise: Promise<TokenResponse> | null = null;
let authStateListener: ((token: TokenResponse | null) => void) | null = null;

export function setAccessToken(token: string | null) {
  accessToken = token;
}

export function setAuthStateListener(listener: (token: TokenResponse | null) => void) {
  authStateListener = listener;
  return () => {
    if (authStateListener === listener) authStateListener = null;
  };
}

http.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

interface RetryableConfig extends InternalAxiosRequestConfig {
  _retried?: boolean;
}

async function refreshAccessToken(): Promise<TokenResponse> {
  if (!refreshPromise) {
    refreshPromise = axios
      .post<ApiResponse<TokenResponse>>('/api/v1/auth/refresh', undefined, { withCredentials: true })
      .then(({ data }) => unwrap(data))
      .then((token) => {
        setAccessToken(token.accessToken);
        authStateListener?.(token);
        return token;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

http.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const config = error.config as RetryableConfig | undefined;
    const url = config?.url ?? '';
    if (error.response?.status === 401 && config && !config._retried
      && !url.includes('/auth/login') && !url.includes('/auth/refresh')) {
      config._retried = true;
      try {
        await refreshAccessToken();
        return http.request(config);
      } catch {
        setAccessToken(null);
        authStateListener?.(null);
      }
    }
    return Promise.reject(error);
  },
);

function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== 'SUCCESS') {
    throw new Error(response.message || '请求失败');
  }
  return response.data;
}

export const apiClient = {
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await http.get<unknown, ApiResponse<T>>(url, config);
    return unwrap(response);
  },
  async post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await http.post<unknown, ApiResponse<T>>(url, data, config);
    return unwrap(response);
  },
  async put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await http.put<unknown, ApiResponse<T>>(url, data, config);
    return unwrap(response);
  },
  async patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await http.patch<unknown, ApiResponse<T>>(url, data, config);
    return unwrap(response);
  },
  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await http.delete<unknown, ApiResponse<T>>(url, config);
    return unwrap(response);
  },
};

export const authClient = {
  async login(username: string, password: string): Promise<TokenResponse> {
    const response = await http.post<unknown, ApiResponse<TokenResponse>>('/auth/login', { username, password });
    const token = unwrap(response);
    setAccessToken(token.accessToken);
    return token;
  },
  refresh: refreshAccessToken,
  async logout(): Promise<void> {
    await http.post<unknown, ApiResponse<void>>('/auth/logout');
    setAccessToken(null);
  },
};

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message;
    return typeof message === 'string' ? message : error.message;
  }
  return error instanceof Error ? error.message : '操作失败，请稍后重试';
}
