/**
 * 模块3：后台用户管理页面。
 * 功能：列表展示、创建、编辑、启用/禁用用户，分配角色以及重置密码。
 * 技术栈：React 18 hooks + Ant Design Table/Form/Modal + 类型化的Axios API客户端。
 */
import { useEffect, useState } from 'react';
import {
  Button,
  Card,
  Form,
  Input,
  message,
  Modal,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { gatewayApi } from '@/api/gateway';
import { getErrorMessage } from '@/api/client';
import type { Role, User } from '@/types/auth';

interface UserFormValues {
  username: string;
  displayName: string;
  password?: string;
  roleIds: number[];
}

export default function UserManagementPage() {
  const [form] = Form.useForm<UserFormValues>();
  const [passwordForm] = Form.useForm<{ newPassword: string }>();
  const [messageApi, contextHolder] = message.useMessage();
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<User | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [resetTarget, setResetTarget] = useState<User | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      const [userPage, rolePage] = await Promise.all([
        gatewayApi.listUsers({ page: 1, size: 100 }),
        gatewayApi.listRoles({ page: 1, size: 100 }),
      ]);
      setUsers(userPage.items);
      setRoles(rolePage.items);
    } catch (error) {
      messageApi.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ roleIds: [] });
    setFormOpen(true);
  };

  const openEdit = (user: User) => {
    setEditing(user);
    form.setFieldsValue({
      username: user.username,
      displayName: user.displayName,
      roleIds: user.roleIds,
    });
    setFormOpen(true);
  };

  const save = async () => {
    const values = await form.validateFields();
    try {
      if (editing) {
        await gatewayApi.updateUser(editing.id, {
          displayName: values.displayName,
          roleIds: values.roleIds ?? [],
          version: editing.version,
        });
      } else {
        await gatewayApi.createUser({
          username: values.username,
          displayName: values.displayName,
          password: values.password ?? '',
          roleIds: values.roleIds ?? [],
        });
      }
      messageApi.success(editing ? '用户已更新' : '用户已创建');
      setFormOpen(false);
      await load();
    } catch (error) {
      messageApi.error(getErrorMessage(error));
    }
  };

  const toggleStatus = async (user: User, enabled: boolean) => {
    try {
      await gatewayApi.updateUserStatus(user.id, enabled ? 'ENABLED' : 'DISABLED', user.version);
      messageApi.success(enabled ? '用户已启用' : '用户已禁用');
      await load();
    } catch (error) {
      messageApi.error(getErrorMessage(error));
    }
  };

  const resetPassword = async () => {
    if (!resetTarget) return;
    const values = await passwordForm.validateFields();
    try {
      await gatewayApi.resetPassword(resetTarget.id, values.newPassword, resetTarget.version);
      messageApi.success('密码已重置');
      setResetTarget(null);
      passwordForm.resetFields();
      await load();
    } catch (error) {
      messageApi.error(getErrorMessage(error));
    }
  };

  const roleName = new Map(roles.map((role) => [role.id, role.name]));
  const columns: ColumnsType<User> = [
    { title: '登录名', dataIndex: 'username', width: 160 },
    { title: '显示名称', dataIndex: 'displayName', width: 160 },
    {
      title: '角色',
      dataIndex: 'roleIds',
      render: (roleIds: number[]) => (
        <Space wrap>{roleIds.map((id) => <Tag key={id}>{roleName.get(id) ?? id}</Tag>)}</Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status) => <Tag color={status === 'ENABLED' ? 'green' : 'default'}>{status}</Tag>,
    },
    {
      title: '启用',
      width: 90,
      render: (_, user) => (
        <Switch checked={user.status === 'ENABLED'} onChange={(checked) => void toggleStatus(user, checked)} />
      ),
    },
    {
      title: '操作',
      width: 180,
      render: (_, user) => (
        <Space>
          <Button type="link" onClick={() => openEdit(user)}>编辑</Button>
          <Button type="link" onClick={() => setResetTarget(user)}>重置密码</Button>
        </Space>
      ),
    },
  ];

  return (
    <>
      {contextHolder}
      <Card
        title="用户管理"
        extra={<Button type="primary" onClick={openCreate}>新增用户</Button>}
      >
        <Typography.Paragraph type="secondary">
          用户通过角色获得权限；密码哈希不会出现在列表或编辑响应中。
        </Typography.Paragraph>
        <Table rowKey="id" loading={loading} columns={columns} dataSource={users} pagination={false} />
      </Card>

      <Modal
        title={editing ? '编辑用户' : '新增用户'}
        open={formOpen}
        onCancel={() => setFormOpen(false)}
        onOk={() => void save()}
        destroyOnClose
      >
        <Form form={form} layout="vertical" preserve={false}>
          <Form.Item name="username" label="登录名" rules={[{ required: true }]}>
            <Input disabled={Boolean(editing)} maxLength={100} />
          </Form.Item>
          <Form.Item name="displayName" label="显示名称" rules={[{ required: true }]}>
            <Input maxLength={100} />
          </Form.Item>
          {!editing && (
            <Form.Item name="password" label="初始密码" rules={[{ required: true, min: 8, max: 72 }]}>
              <Input.Password autoComplete="new-password" />
            </Form.Item>
          )}
          <Form.Item name="roleIds" label="角色">
            <Select
              mode="multiple"
              options={roles.filter((role) => role.status === 'ENABLED').map((role) => ({
                value: role.id,
                label: `${role.name} (${role.code})`,
              }))}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`重置密码${resetTarget ? `：${resetTarget.username}` : ''}`}
        open={Boolean(resetTarget)}
        onCancel={() => setResetTarget(null)}
        onOk={() => void resetPassword()}
        destroyOnClose
      >
        <Form form={passwordForm} layout="vertical" preserve={false}>
          <Form.Item name="newPassword" label="新密码" rules={[{ required: true, min: 8, max: 72 }]}>
            <Input.Password autoComplete="new-password" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
