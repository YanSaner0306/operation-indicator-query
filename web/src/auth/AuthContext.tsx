/**
 * 模块5：前端登录态与权限上下文。
 * 功能：启动时用刷新 Cookie 恢复会话，向页面提供登录、退出和权限判断能力。
 * 技术栈：React Context、Hooks、Axios 内存令牌管理。
 */
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { authClient, setAuthStateListener } from '@/api/client';
import type { CurrentPrincipal, TokenResponse } from '@/types/auth';

interface AuthContextValue {
  principal: CurrentPrincipal | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  hasPermission: (permission: string) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [principal, setPrincipal] = useState<CurrentPrincipal | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => setAuthStateListener((token: TokenResponse | null) => {
    setPrincipal(token?.principal ?? null);
  }), []);

  useEffect(() => {
    authClient.refresh()
      .then((token) => setPrincipal(token.principal))
      .catch(() => setPrincipal(null))
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const token = await authClient.login(username, password);
    setPrincipal(token.principal);
  }, []);

  const logout = useCallback(async () => {
    try {
      await authClient.logout();
    } finally {
      setPrincipal(null);
    }
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    principal,
    loading,
    login,
    logout,
    hasPermission: (permission) => principal?.permissions.includes(permission) ?? false,
  }), [principal, loading, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth 必须在 AuthProvider 内使用');
  return context;
}
