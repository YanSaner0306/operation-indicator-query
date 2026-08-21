import { apiClient, type PageResponse } from './client';
import type {
  ConfigStatus,
  Ontology,
  OntologyGraph,
  OntologyPayload,
  OntologyProperty,
  OntologyPropertyPayload,
  OntologyRelation,
  OntologyRelationPayload,
} from '@/types/ontology';

export const ontologyApi = {
  page: (params: {
    domainId?: number;
    unclassified?: boolean;
    keyword?: string;
    status?: ConfigStatus;
    page: number;
    size: number;
  }) => apiClient.get<PageResponse<Ontology>>('/ontologies', { params }),
  get: (id: number) => apiClient.get<Ontology>(`/ontologies/${id}`),
  create: (payload: OntologyPayload) => apiClient.post<Ontology>('/ontologies', payload),
  update: (id: number, payload: OntologyPayload) => apiClient.put<Ontology>(`/ontologies/${id}`, payload),
  updateStatus: (id: number, status: ConfigStatus, version: number) =>
    apiClient.patch<Ontology>(`/ontologies/${id}/status`, { status, version }),
  remove: (id: number) => apiClient.delete<void>(`/ontologies/${id}`),
  properties: (ontologyId: number) =>
    apiClient.get<OntologyProperty[]>(`/ontologies/${ontologyId}/properties`),
  createProperty: (ontologyId: number, payload: OntologyPropertyPayload) =>
    apiClient.post<OntologyProperty>(`/ontologies/${ontologyId}/properties`, payload),
  updateProperty: (ontologyId: number, propertyId: number, payload: OntologyPropertyPayload) =>
    apiClient.put<OntologyProperty>(`/ontologies/${ontologyId}/properties/${propertyId}`, payload),
  removeProperty: (ontologyId: number, propertyId: number) =>
    apiClient.delete<void>(`/ontologies/${ontologyId}/properties/${propertyId}`),
  relations: (ontologyId: number) =>
    apiClient.get<OntologyRelation[]>(`/ontologies/${ontologyId}/relations`),
  createRelation: (ontologyId: number, payload: OntologyRelationPayload) =>
    apiClient.post<OntologyRelation>(`/ontologies/${ontologyId}/relations`, payload),
  updateRelation: (ontologyId: number, relationId: number, payload: OntologyRelationPayload) =>
    apiClient.put<OntologyRelation>(`/ontologies/${ontologyId}/relations/${relationId}`, payload),
  removeRelation: (ontologyId: number, relationId: number) =>
    apiClient.delete<void>(`/ontologies/${ontologyId}/relations/${relationId}`),
  graph: (domainId?: number) => apiClient.get<OntologyGraph>('/ontology-graph', { params: { domainId } }),
};
