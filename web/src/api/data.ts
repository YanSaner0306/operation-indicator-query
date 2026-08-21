/**
 * 模块6-8：数据源管理前端API封装。
 * 功能：调用配置CRUD、连接测试、元数据读取和安全预览接口。
 * 技术栈：TypeScript与统一Axios客户端。
 */
import { apiClient, type PageResponse } from './client';
import type { ColumnMetadata, ConfigStatus, DataSourceConfig, PreviewResult, SaveDataSourcePayload, TableMetadata } from '@/types/data-source';
import type { Binding, BindingPayload, BindingPreview, BindingValidation } from '@/types/binding';

export const dataApi = {
  page: (params?: { keyword?: string; status?: ConfigStatus; page?: number; size?: number }) =>
    apiClient.get<PageResponse<DataSourceConfig>>('/data-sources', { params }),
  create: (data: SaveDataSourcePayload) => apiClient.post<DataSourceConfig>('/data-sources', data),
  update: (id: number, data: SaveDataSourcePayload) => apiClient.put<DataSourceConfig>(`/data-sources/${id}`, data),
  setStatus: (item: DataSourceConfig, status: ConfigStatus) =>
    apiClient.patch<DataSourceConfig>(`/data-sources/${item.id}/enabled`, { status, version: item.version }),
  remove: (item: DataSourceConfig) => apiClient.delete<void>(`/data-sources/${item.id}`, { params: { version: item.version } }),
  test: (id: number) => apiClient.post<{ success: boolean; latencyMs: number; message: string }>(`/data-sources/${id}/test`),
  tables: (id: number) => apiClient.get<TableMetadata[]>(`/data-sources/${id}/tables`),
  columns: (id: number, table: string) => apiClient.get<ColumnMetadata[]>(`/data-sources/${id}/tables/${encodeURIComponent(table)}/columns`),
  preview: (id: number, table: string, columns: string[], limit = 20) =>
    apiClient.post<PreviewResult>(`/data-sources/${id}/tables/${encodeURIComponent(table)}/preview`, { columns, limit }),
};

export const bindingApi = {
  page: (params: { keyword?: string; ontologyId?: number; dataSourceId?: number; status?: ConfigStatus; page?: number; size?: number }) => apiClient.get<PageResponse<Binding>>('/bindings', { params }),
  get: (id: number) => apiClient.get<Binding>(`/bindings/${id}`),
  create: (payload: BindingPayload) => apiClient.post<Binding>('/bindings', payload),
  update: (id: number, payload: BindingPayload) => apiClient.put<Binding>(`/bindings/${id}`, payload),
  validate: (id: number) => apiClient.post<BindingValidation>(`/bindings/${id}/validate`),
  preview: (id: number) => apiClient.post<BindingPreview>(`/bindings/${id}/preview`),
  setStatus: (item: Binding, status: ConfigStatus) => apiClient.patch<Binding>(`/bindings/${item.id}/enabled`, { status, version: item.version }),
  remove: (item: Binding) => apiClient.delete<void>(`/bindings/${item.id}`, { params: { version: item.version } }),
};
