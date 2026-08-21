import type { PropertyDataType } from './ontology';

export type RuleOperator =
  | 'EQ'
  | 'NE'
  | 'GT'
  | 'GE'
  | 'LT'
  | 'LE'
  | 'CONTAINS'
  | 'NOT_CONTAINS'
  | 'IS_EMPTY'
  | 'IS_NOT_EMPTY'
  | 'BEFORE'
  | 'AFTER';

export interface RuleCondition {
  propertyId: number;
  propertyName: string;
  operator: RuleOperator;
  compareValue?: string;
  valueType: PropertyDataType;
}

export interface RuleAction {
  actionType: 'RETURN_RESULT';
  resultCode: string;
  resultName: string;
  message?: string;
}

export interface Rule {
  id: number;
  name: string;
  code: string;
  ontologyId: number;
  ontologyName: string;
  description?: string;
  enabled: boolean;
  currentVersionId: number;
  currentVersionNo: number;
  condition: RuleCondition;
  action: RuleAction;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface RuleVersion {
  id: number;
  versionNo: number;
  changeNote?: string;
  createdBy?: string;
  createdAt: string;
  condition: RuleCondition;
  action: RuleAction;
}

export interface RulePayload {
  name: string;
  code: string;
  ontologyId?: number;
  description?: string;
  enabled?: boolean;
  condition: {
    propertyId: number;
    operator: RuleOperator;
    compareValue?: unknown;
  };
  action: {
    resultCode: string;
    resultName: string;
    message?: string;
  };
  changeNote?: string;
  createdBy?: string;
  version?: number;
}

export interface RuleTestResult {
  ruleId: number;
  versionNo: number;
  matched: boolean;
  condition: {
    propertyId: number;
    propertyName: string;
    actualValue: unknown;
    operator: RuleOperator;
    expectedValue?: string;
  };
  action?: RuleAction;
}
