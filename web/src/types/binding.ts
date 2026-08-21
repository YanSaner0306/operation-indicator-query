/**
 * 模块9-13：数据绑定、字段映射、筛选条件及校验预览的数据契约。
 * 功能：保证绑定管理向导与后端 DTO 字段一致，并限制筛选操作符的可选范围。
 * 技术栈：TypeScript 严格类型、联合类型与嵌套接口。
 */
import type { ConfigStatus } from './ontology';

export type BindingFilterOperator = 'EQ'|'NE'|'GT'|'GE'|'LT'|'LE'|'IN'|'IS_NULL'|'NOT_NULL';
export interface BindingMapping { id?: number; sourceColumn: string; sourceDataType?: string; ontologyPropertyId: number; propertyCode?: string; propertyName?: string; uniqueKey?: boolean; }
export interface BindingFilter { id?: number; sourceColumn: string; sourceDataType?: string; operator: BindingFilterOperator; value?: string; sequence?: number; }
export interface BindingPayload { name: string; dataSourceId: number; schemaName?: string; tableName: string; ontologyId: number; mappings: BindingMapping[]; filters: BindingFilter[]; version?: number; }
export interface Binding extends BindingPayload { id: number; dataSourceName: string; ontologyName: string; status: ConfigStatus; lastTestStatus: 'NOT_TESTED'|'SUCCESS'|'FAILED'; lastTestAt?: string; createdAt: string; updatedAt: string; version: number; }
export interface BindingValidation { valid: boolean; messages: string[]; }
export interface BindingPreview { ontologyId: number; externalKey: unknown; sourceValues: Record<string, unknown>; properties: Record<string, unknown>; durationMs: number; }
