/**
 * 模块1-8：管理控制台外壳和权限菜单。
 * 功能：按当前用户权限过滤导航，并提供用户信息与安全退出入口。
 * 技术栈：React 18、Ant Design Layout/Menu、React Router 与 RBAC 上下文。
 */
import { Button, Layout, Menu, Space, Typography } from 'antd';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '@/auth/AuthContext';

const { Header, Sider, Content } = Layout;

const items = [
  { key: '/dashboard', label: '总览' },
  { key: '/ontology', label: '本体管理', permission: 'ONTOLOGY_VIEW' },
  { key: '/rules', label: '规则管理', permission: 'RULE_VIEW' },
  { key: '/strategy', label: '策略管理' },
  { key: '/risk', label: '风险管理' },
  { key: '/data', label: '数据管理', permission: 'DATASOURCE_VIEW', children: [
    { key: '/data/sources', label: '数据源' },
    { key: '/data/bindings', label: '本体数据绑定' },
  ] },
  {
    key: '/gateway',
    label: '对接管理',
    children: [
      { key: '/gateway/users', label: '用户管理' },
      { key: '/gateway/roles', label: '角色与权限' },
      { key: '/gateway/api-clients', label: 'API 客户端' },
      { key: '/gateway/audit-logs', label: '审计日志' },
    ],
  },
  { key: '/evaluate', label: '测试评估' },
];

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { principal, hasPermission, logout } = useAuth();
  const visibleItems = items.filter((item) => !item.permission || hasPermission(item.permission));
  const selected = location.pathname.startsWith('/gateway/') || location.pathname.startsWith('/data/')
    ? location.pathname
    : visibleItems.find((item) => location.pathname.startsWith(item.key))?.key ?? '/dashboard';

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header className="app-header">
        <div>
          <Typography.Text className="app-title">业务本体推理平台</Typography.Text>
          <Typography.Text className="app-version">v0.1 · 即时查询架构</Typography.Text>
        </div>
        <Space>
          <Typography.Text style={{ color: '#fff' }}>{principal?.displayName}</Typography.Text>
          <Button size="small" onClick={() => logout().finally(() => navigate('/login', { replace: true }))}>
            退出登录
          </Button>
        </Space>
      </Header>
      <Layout>
        <Sider width={208} theme="light" className="app-sider">
          <Menu
            mode="inline"
            selectedKeys={[selected]}
            defaultOpenKeys={[...(location.pathname.startsWith('/gateway') ? ['/gateway'] : []), ...(location.pathname.startsWith('/data') ? ['/data'] : [])]}
            items={visibleItems}
            onClick={({ key }) => navigate(key)}
            style={{ borderRight: 0, paddingTop: 12 }}
          />
        </Sider>
        <Content className="app-content"><Outlet /></Content>
      </Layout>
    </Layout>
  );
}
