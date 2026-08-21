/**
 * 模块14-15：API 客户端、凭证和审计日志的管理端请求封装。
 * 功能：统一分页、启停、密钥轮换撤销及审计筛选接口调用。
 * 技术栈：Axios 共享客户端、TypeScript DTO 与 REST API。
 */
import { apiClient, type PageResponse } from './client';
import type { Permission, AuthStatus } from '@/types/auth';
import type { ApiClient, ApiClientPayload, AuditLog, CreatedApiKey } from '@/types/api-client';

export const machineClientApi = {
  page: (params: { keyword?: string; status?: AuthStatus; page?: number; size?: number }) => apiClient.get<PageResponse<ApiClient>>('/auth/api-clients', { params }),
  create: (payload: ApiClientPayload) => apiClient.post<ApiClient>('/auth/api-clients', payload),
  update: (id: number, payload: ApiClientPayload) => apiClient.put<ApiClient>(`/auth/api-clients/${id}`, payload),
  setStatus: (item: ApiClient, status: AuthStatus) => apiClient.patch<ApiClient>(`/auth/api-clients/${item.id}/enabled`, { status, version: item.version }),
  createCredential: (id: number, expiresAt?: string, revokeExisting = true) => apiClient.post<CreatedApiKey>(`/auth/api-clients/${id}/credentials`, { expiresAt, revokeExisting }),
  revoke: (id: number, keyId: string) => apiClient.post<void>(`/auth/api-clients/${id}/credentials/${keyId}/revoke`),
  permissions: () => apiClient.get<Permission[]>('/auth/permissions'),
};
export const auditLogApi = {
  page: (params: { requestId?: string; principalId?: string; action?: string; result?: string; from?: string; to?: string; page?: number; size?: number }) => apiClient.get<PageResponse<AuditLog>>('/auth/audit-logs', { params }),
};
