/**
 * 模块1-8：应用路由、登录守卫与页面级权限矩阵。
 * 功能：保持本体和规则页面不变，并限制匿名访问和越权访问。
 * 技术栈：React Router 6 数据路由与 RBAC 路由守卫。
 */
import { createBrowserRouter, Navigate } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import Dashboard from '@/pages/Dashboard';
import LoginPage from '@/pages/login';
import { LoginGuard, PermissionGuard } from '@/auth/RouteGuard';
import OntologyPage from '@/pages/ontology';
import OntologyDetailPage from '@/pages/ontology/detail';
import OntologyGraphPage from '@/pages/ontology/graph';
import RulePage from '@/pages/rule';
import RuleEditorPage from '@/pages/rule/editor';
import RuleTestPage from '@/pages/rule/test';
import StrategyPage from '@/pages/strategy';
import RiskPage from '@/pages/risk';
import DataPage from '@/pages/data';
import BindingListPage from '@/pages/data/bindings';
import BindingEditorPage from '@/pages/data/binding-editor';
import GatewayPage from '@/pages/gateway';
import UserManagementPage from '@/pages/gateway/users';
import RoleManagementPage from '@/pages/gateway/roles';
import ApiClientPage from '@/pages/gateway/api-clients';
import EvaluatePage from '@/pages/evaluate';
import AuditPage from '@/pages/audit';

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <LoginGuard />,
    children: [{
      path: '/',
      element: <MainLayout />,
      children: [
        { index: true, element: <Navigate to="/dashboard" replace /> },
        { path: 'dashboard', element: <Dashboard /> },
        { path: 'domain', element: <Navigate to="/ontology" replace /> },
        {
          element: <PermissionGuard permission="ONTOLOGY_VIEW" />,
          children: [
            { path: 'ontology', element: <OntologyPage /> },
            { path: 'ontology/:id', element: <OntologyDetailPage /> },
            { path: 'ontology/:id/graph', element: <OntologyGraphPage /> },
          ],
        },
        {
          element: <PermissionGuard permission="RULE_VIEW" />,
          children: [
            { path: 'rule', element: <RulePage /> },
            { path: 'rules', element: <RulePage /> },
            { path: 'rules/new', element: <RuleEditorPage /> },
            { path: 'rules/:id/edit', element: <RuleEditorPage /> },
            { path: 'rules/:id/test', element: <RuleTestPage /> },
          ],
        },
        { path: 'strategy', element: <StrategyPage /> },
        { path: 'risk', element: <RiskPage /> },
        {
          element: <PermissionGuard permission="DATASOURCE_VIEW" />,
          children: [{ path: 'data', element: <Navigate to="/data/sources" replace /> }, { path: 'data/sources', element: <DataPage /> }],
        },
        {
          element: <PermissionGuard permission="BINDING_VIEW" />,
          children: [{ path: 'data/bindings', element: <BindingListPage /> }, { path: 'data/bindings/new', element: <BindingEditorPage /> }, { path: 'data/bindings/:id/edit', element: <BindingEditorPage /> }],
        },
        { path: 'gateway', element: <GatewayPage /> },
        { path: 'gateway/users', element: <UserManagementPage /> },
        { path: 'gateway/roles', element: <RoleManagementPage /> },
        { element: <PermissionGuard permission="AUTH_MANAGE" />, children: [{ path: 'gateway/api-clients', element: <ApiClientPage /> }] },
        { path: 'evaluate', element: <EvaluatePage /> },
        { path: 'audit', element: <Navigate to="/gateway/audit-logs" replace /> },
        { element: <PermissionGuard permission="AUDIT_VIEW" />, children: [{ path: 'gateway/audit-logs', element: <AuditPage /> }] },
      ],
    }],
  },
]);
