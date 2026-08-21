/**
 * 模块5：按钮级权限组件。
 * 功能：仅在当前用户拥有指定权限时渲染新增、编辑、删除等操作控件。
 * 技术栈：React 条件渲染与 RBAC 权限上下文。
 */
import type { ReactNode } from 'react';
import { useAuth } from './AuthContext';

export default function Permission({ code, children }: { code: string; children: ReactNode }) {
  return useAuth().hasPermission(code) ? <>{children}</> : null;
}
