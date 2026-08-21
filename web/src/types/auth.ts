/**
 * 模块3-5：前端身份、角色、权限与令牌数据契约。
 * 功能：统一用户管理数据和登录态数据结构，保证前后端 DTO 字段一致。
 * 技术栈：TypeScript 严格模式接口与联合类型。
 */
export type AuthStatus = 'ENABLED' | 'DISABLED';

export interface User {
  id: number;
  username: string;
  displayName: string;
  status: AuthStatus;
  roleIds: number[];
  lastLoginAt?: string;
  lockedUntil?: string;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface Role {
  id: number;
  code: string;
  name: string;
  status: AuthStatus;
  permissionCodes: string[];
  userCount: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface Permission {
  id: number;
  code: string;
  name: string;
  module: string;
}

export interface CreateUserPayload {
  username: string;
  displayName: string;
  password: string;
  roleIds: number[];
}

export interface UpdateUserPayload {
  displayName: string;
  roleIds: number[];
  version: number;
}

export interface CreateRolePayload {
  code: string;
  name: string;
  permissionCodes: string[];
}

export interface UpdateRolePayload {
  name: string;
  version: number;
}

export interface CurrentPrincipal {
  userId: number;
  username: string;
  displayName: string;
  permissions: string[];
}

export interface TokenResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  principal: CurrentPrincipal;
}
