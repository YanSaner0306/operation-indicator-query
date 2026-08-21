import type { DomainNode, PropertyDataType } from '@/types/ontology';
import type { RuleOperator } from '@/types/rule';

export interface FlatDomain extends DomainNode {
  level: number;
}

export function flattenDomains(nodes: DomainNode[], level = 0): FlatDomain[] {
  return nodes.flatMap((node) => [
    { ...node, level },
    ...flattenDomains(node.children ?? [], level + 1),
  ]);
}

export const statusText = {
  ENABLED: '启用',
  DISABLED: '禁用',
} as const;

export const propertyTypeText: Record<PropertyDataType, string> = {
  STRING: '字符串',
  INTEGER: '整数',
  DECIMAL: '小数',
  BOOLEAN: '布尔',
  DATE: '日期',
  DATETIME: '日期时间',
  ENUM: '枚举',
};

export const operatorText: Record<RuleOperator, string> = {
  EQ: '等于',
  NE: '不等于',
  GT: '大于',
  GE: '大于等于',
  LT: '小于',
  LE: '小于等于',
  CONTAINS: '包含',
  NOT_CONTAINS: '不包含',
  IS_EMPTY: '为空',
  IS_NOT_EMPTY: '不为空',
  BEFORE: '早于',
  AFTER: '晚于',
};

export function operatorsForType(type?: PropertyDataType): RuleOperator[] {
  switch (type) {
    case 'INTEGER':
    case 'DECIMAL':
      return ['EQ', 'NE', 'GT', 'GE', 'LT', 'LE'];
    case 'STRING':
      return ['EQ', 'NE', 'CONTAINS', 'NOT_CONTAINS', 'IS_EMPTY', 'IS_NOT_EMPTY'];
    case 'DATE':
    case 'DATETIME':
      return ['EQ', 'BEFORE', 'AFTER'];
    case 'BOOLEAN':
    case 'ENUM':
      return ['EQ', 'NE'];
    default:
      return [];
  }
}

export function formatDateTime(value?: string): string {
  if (!value) return '-';
  return new Date(value).toLocaleString('zh-CN', { hour12: false });
}
