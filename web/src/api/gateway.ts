/**
 * Module 3: RBAC management API client.
 * Function: calls user, role, permission, status and password-reset endpoints.
 * Stack: TypeScript DTOs over the shared Axios apiClient.
 */
import { apiClient, type PageResponse } from './client';
import type {
  AuthStatus,
  CreateRolePayload,
  CreateUserPayload,
  Permission,
  Role,
  UpdateRolePayload,
  UpdateUserPayload,
  User,
} from '@/types/auth';

export interface AuthPageQuery {
  keyword?: string;
  status?: AuthStatus;
  page?: number;
  size?: number;
}

export const gatewayApi = {
  listUsers: (params: AuthPageQuery) => apiClient.get<PageResponse<User>>('/auth/users', { params }),
  createUser: (payload: CreateUserPayload) => apiClient.post<User>('/auth/users', payload),
  updateUser: (id: number, payload: UpdateUserPayload) => apiClient.put<User>(`/auth/users/${id}`, payload),
  updateUserStatus: (id: number, status: AuthStatus, version: number) =>
    apiClient.patch<User>(`/auth/users/${id}/enabled`, { status, version }),
  resetPassword: (id: number, newPassword: string, version: number) =>
    apiClient.post<User>(`/auth/users/${id}/password/reset`, { newPassword, version }),

  listRoles: (params: AuthPageQuery) => apiClient.get<PageResponse<Role>>('/auth/roles', { params }),
  createRole: (payload: CreateRolePayload) => apiClient.post<Role>('/auth/roles', payload),
  updateRole: (id: number, payload: UpdateRolePayload) => apiClient.put<Role>(`/auth/roles/${id}`, payload),
  updateRoleStatus: (id: number, status: AuthStatus, version: number) =>
    apiClient.patch<Role>(`/auth/roles/${id}/enabled`, { status, version }),
  setRolePermissions: (id: number, permissionCodes: string[], version: number) =>
    apiClient.put<Role>(`/auth/roles/${id}/permissions`, { permissionCodes, version }),
  listPermissions: () => apiClient.get<Permission[]>('/auth/permissions'),
};
