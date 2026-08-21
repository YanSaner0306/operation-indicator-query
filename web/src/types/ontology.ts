export type ConfigStatus = 'ENABLED' | 'DISABLED';
export type DomainStatus = ConfigStatus;

export interface DomainNode {
  id: number;
  parentId: number | null;
  name: string;
  code: string;
  description?: string;
  status: DomainStatus;
  sortOrder: number;
  version: number;
  children: DomainNode[];
}

export interface DomainPayload {
  parentId?: number | null;
  name: string;
  code: string;
  description?: string;
  status: DomainStatus;
  sortOrder: number;
  version?: number;
}

export interface ParentDomainPayload {
  name: string;
  code: string;
  description?: string;
  status: DomainStatus;
  sortOrder: number;
  childDomainIds: number[];
}

export interface Ontology {
  id: number;
  name: string;
  code: string;
  description?: string;
  status: ConfigStatus;
  domainIds: number[];
  propertyCount: number;
  relationCount: number;
  ruleCount: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface OntologyPayload {
  name: string;
  code: string;
  description?: string;
  status: ConfigStatus;
  domainIds: number[];
  version?: number;
}

export type PropertyDataType =
  | 'STRING'
  | 'INTEGER'
  | 'DECIMAL'
  | 'BOOLEAN'
  | 'DATE'
  | 'DATETIME'
  | 'ENUM';

export interface OntologyProperty {
  id: number;
  ontologyId: number;
  name: string;
  code: string;
  dataType: PropertyDataType;
  length?: number;
  precision?: number;
  scale?: number;
  required: boolean;
  uniqueFlag: boolean;
  defaultValue?: string;
  description?: string;
  sortOrder: number;
  status: ConfigStatus;
  createdAt: string;
  updatedAt: string;
}

export interface OntologyPropertyPayload {
  name: string;
  code: string;
  dataType: PropertyDataType;
  length?: number;
  precision?: number;
  scale?: number;
  required: boolean;
  unique: boolean;
  defaultValue?: string;
  description?: string;
  sortOrder: number;
  status: ConfigStatus;
}

export type RelationCardinality =
  | 'ONE_TO_ONE'
  | 'ONE_TO_MANY'
  | 'MANY_TO_ONE'
  | 'MANY_TO_MANY';

export interface OntologyRelation {
  id: number;
  sourceOntologyId: number;
  targetOntologyId: number;
  name: string;
  code: string;
  cardinality: RelationCardinality;
  sourcePropertyId?: number;
  targetPropertyId?: number;
  description?: string;
  status: ConfigStatus;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface OntologyRelationPayload {
  name: string;
  code: string;
  targetOntologyId: number;
  cardinality: RelationCardinality;
  sourcePropertyId?: number;
  targetPropertyId?: number;
  description?: string;
  status: ConfigStatus;
  version?: number;
}

export interface OntologyGraph {
  nodes: Array<{ id: number; name: string; code: string }>;
  edges: Array<{
    id: number;
    sourceOntologyId: number;
    targetOntologyId: number;
    name: string;
    code: string;
    cardinality: RelationCardinality;
  }>;
}
