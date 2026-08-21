/**
 * 模块5：页面级登录与权限守卫。
 * 功能：阻止匿名用户进入后台，并对缺少指定权限的用户展示 403 页面。
 * 技术栈：React Router 6、Ant Design Result 与 React Context。
 */
import { Result, Spin } from 'antd';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

export function LoginGuard() {
  const { principal, loading } = useAuth();
  const location = useLocation();
  if (loading) return <Spin fullscreen tip="正在恢复登录状态" />;
  if (!principal) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return <Outlet />;
}

export function PermissionGuard({ permission }: { permission: string }) {
  const { hasPermission } = useAuth();
  return hasPermission(permission)
    ? <Outlet />
    : <Result status="403" title="403" subTitle="当前账号没有访问此页面的权限" />;
}
