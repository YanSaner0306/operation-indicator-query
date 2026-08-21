import { apiClient, type PageResponse } from './client';
import type { Rule, RulePayload, RuleTestResult, RuleVersion } from '@/types/rule';

export const ruleApi = {
  page: (params: {
    keyword?: string;
    ontologyId?: number;
    enabled?: boolean;
    page: number;
    size: number;
  }) => apiClient.get<PageResponse<Rule>>('/rules', { params }),
  get: (id: number) => apiClient.get<Rule>(`/rules/${id}`),
  create: (payload: RulePayload) => apiClient.post<Rule>('/rules', payload),
  update: (id: number, payload: RulePayload) => apiClient.put<Rule>(`/rules/${id}`, payload),
  updateEnabled: (id: number, enabled: boolean, version: number) =>
    apiClient.patch<Rule>(`/rules/${id}/enabled`, { enabled, version }),
  remove: (id: number) => apiClient.delete<void>(`/rules/${id}`),
  versions: (ruleId: number) => apiClient.get<RuleVersion[]>(`/rules/${ruleId}/versions`),
  switchVersion: (ruleId: number, versionId: number, version: number) =>
    apiClient.post<Rule>(`/rules/${ruleId}/versions/${versionId}/switch`, { version }),
  test: (ruleId: number, versionId: number | undefined, values: Record<string, unknown>) =>
    apiClient.post<RuleTestResult>(`/rules/${ruleId}/test`, { versionId, values }),
};
