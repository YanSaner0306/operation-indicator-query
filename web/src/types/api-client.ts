/**
 * 模块14：API 客户端、权限和机器凭证的数据契约。
 * 功能：区分只返回摘要的持久化凭证与只展示一次的完整 API Key。
 * 技术栈：TypeScript 接口、联合类型与只读响应建模。
 */
import type { AuthStatus } from './auth';
export interface ApiCredential { id: number; keyId: string; keyPrefix: string; status: 'ACTIVE'|'REVOKED'|'EXPIRED'; expiresAt?: string; lastUsedAt?: string; createdAt: string; }
export interface ApiClient { id: number; clientId: string; name: string; status: AuthStatus; permissionCodes: string[]; credentials: ApiCredential[]; lastUsedAt?: string; version: number; createdAt: string; updatedAt: string; }
export interface ApiClientPayload { clientId: string; name: string; permissionCodes: string[]; version?: number; }
export interface CreatedApiKey { clientId: string; keyId: string; keyPrefix: string; apiKey: string; expiresAt: string; }
export interface AuditLog { id: number; requestId: string; principalType?: string; principalId?: string; action: string; resourceType?: string; resourceId?: string; result: 'SUCCESS'|'FAILED'|'DENIED'; httpMethod: string; path: string; durationMs: number; clientIp?: string; errorCode?: string; createdAt: string; }
