import { apiClient, type PageResponse } from './client';
import type { DomainNode, DomainPayload, DomainStatus, ParentDomainPayload } from '@/types/ontology';

export const domainApi = {
  tree: () => apiClient.get<DomainNode[]>('/domains/tree'),
  page: (params?: { keyword?: string; status?: DomainStatus; page?: number; size?: number }) =>
    apiClient.get<PageResponse<DomainNode>>('/domains', { params }),
  get: (id: number) => apiClient.get<DomainNode>(`/domains/${id}`),
  create: (payload: DomainPayload) => apiClient.post<DomainNode>('/domains', payload),
  createParent: (payload: ParentDomainPayload) => apiClient.post<DomainNode>('/domains/parents', payload),
  update: (id: number, payload: DomainPayload) => apiClient.put<DomainNode>(`/domains/${id}`, payload),
  remove: (id: number) => apiClient.delete<void>(`/domains/${id}`),
};
