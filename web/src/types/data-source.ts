/**
 * 模块6-8：前端数据源、元数据和预览契约。
 * 功能：约束配置表单、连接状态、表列信息和预览结果的数据结构。
 * 技术栈：TypeScript严格模式接口与联合类型。
 */
export type ConfigStatus = 'ENABLED' | 'DISABLED';
export type DatabaseType = 'MYSQL' | 'H2';
export type TestStatus = 'UNTESTED' | 'SUCCESS' | 'FAILED';

export interface DataSourceConfig {
  id: number; name: string; dbType: DatabaseType; host: string; port: number;
  databaseName: string; username: string; status: ConfigStatus; lastTestStatus: TestStatus;
  lastTestAt?: string; version: number; createdAt: string; updatedAt: string;
}
export interface SaveDataSourcePayload {
  name: string; dbType: DatabaseType; host: string; port: number; databaseName: string;
  username: string; password?: string; version?: number;
}
export interface TableMetadata { name: string; type: string; remarks?: string; }
export interface ColumnMetadata { name: string; typeName: string; jdbcType: number; nullable: boolean; primaryKey: boolean; }
export interface PreviewResult { columns: string[]; rows: Record<string, unknown>[]; limit: number; }
