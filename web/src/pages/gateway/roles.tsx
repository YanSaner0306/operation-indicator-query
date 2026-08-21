/**
 * 模块3：角色和权限管理页面。
 * 功能：列表展示、创建、编辑、启用/禁用角色，以及分配后端字典权限。
 * 技术栈：React 18 hooks + Ant Design Table/Form/Modal + 类型化的Axios API客户端。
 */
import { useEffect, useMemo, useState } from 'react';
import { Button, Card, Checkbox, Form, Input, message, Modal, Space, Switch, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { gatewayApi } from '@/api/gateway';
import { getErrorMessage } from '@/api/client';
import type { Permission, Role } from '@/types/auth';

interface RoleFormValues {
  code: string;
  name: string;
  permissionCodes: string[];
}

export default function RoleManagementPage() {
  const [form] = Form.useForm<RoleFormValues>();
  const [messageApi, contextHolder] = message.useMessage();
  const [roles, setRoles] = useState<Role[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<Role | null>(null);
  const [formOpen, setFormOpen] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const [rolePage, dictionary] = await Promise.all([
        gatewayApi.listRoles({ page: 1, size: 100 }),
        gatewayApi.listPermissions(),
      ]);
      setRoles(rolePage.items);
      setPermissions(dictionary);
    } catch (error) {
      messageApi.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const groupedPermissions = useMemo(() => {
    return permissions.reduce<Record<string, Permission[]>>((groups, permission) => {
      groups[permission.module] = [...(groups[permission.module] ?? []), permission];
      return groups;
    }, {});
  }, [permissions]);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ permissionCodes: [] });
    setFormOpen(true);
  };

  const openEdit = (role: Role) => {
    setEditing(role);
    form.setFieldsValue({ code: role.code, name: role.name, permissionCodes: role.permissionCodes });
    setFormOpen(true);
  };

  const save = async () => {
    const values = await form.validateFields();
    try {
      if (editing) {
        const updated = await gatewayApi.updateRole(editing.id, { name: values.name, version: editing.version });
        await gatewayApi.setRolePermissions(updated.id, values.permissionCodes ?? [], updated.version);
      } else {
        await gatewayApi.createRole({
          code: values.code.toUpperCase(),
          name: values.name,
          permissionCodes: values.permissionCodes ?? [],
        });
      }
      messageApi.success(editing ? '角色已更新' : '角色已创建');
      setFormOpen(false);
      await load();
    } catch (error) {
      messageApi.error(getErrorMessage(error));
    }
  };

  const toggleStatus = async (role: Role, enabled: boolean) => {
    try {
      await gatewayApi.updateRoleStatus(role.id, enabled ? 'ENABLED' : 'DISABLED', role.version);
      messageApi.success(enabled ? '角色已启用' : '角色已禁用');
      await load();
    } catch (error) {
      messageApi.error(getErrorMessage(error));
    }
  };

  const columns: ColumnsType<Role> = [
    { title: '角色编码', dataIndex: 'code', width: 180 },
    { title: '角色名称', dataIndex: 'name', width: 160 },
    {
      title: '权限',
      dataIndex: 'permissionCodes',
      render: (codes: string[]) => <Space wrap>{codes.map((code) => <Tag key={code}>{code}</Tag>)}</Space>,
    },
    { title: '关联用户', dataIndex: 'userCount', width: 100 },
    {
      title: '启用',
      width: 90,
      render: (_, role) => (
        <Switch checked={role.status === 'ENABLED'} onChange={(checked) => void toggleStatus(role, checked)} />
      ),
    },
    {
      title: '操作',
      width: 90,
      render: (_, role) => <Button type="link" onClick={() => openEdit(role)}>编辑</Button>,
    },
  ];

  return (
    <>
      {contextHolder}
      <Card title="角色与权限" extra={<Button type="primary" onClick={openCreate}>新增角色</Button>}>
        <Typography.Paragraph type="secondary">
          权限只能从后端字典中选择，前端不能提交自定义权限码。
        </Typography.Paragraph>
        <Table rowKey="id" loading={loading} columns={columns} dataSource={roles} pagination={false} />
      </Card>

      <Modal
        title={editing ? '编辑角色' : '新增角色'}
        open={formOpen}
        onCancel={() => setFormOpen(false)}
        onOk={() => void save()}
        width={720}
        destroyOnClose
      >
        <Form form={form} layout="vertical" preserve={false}>
          <Form.Item
            name="code"
            label="角色编码"
            rules={[{ required: true, pattern: /^[A-Z][A-Z0-9_]{2,63}$/ }]}
          >
            <Input disabled={Boolean(editing)} maxLength={64} onChange={(event) => {
              form.setFieldValue('code', event.target.value.toUpperCase());
            }} />
          </Form.Item>
          <Form.Item name="name" label="角色名称" rules={[{ required: true, max: 100 }]}>
            <Input />
          </Form.Item>
          <Form.Item name="permissionCodes" label="权限点">
            <Checkbox.Group style={{ width: '100%' }}>
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                {Object.entries(groupedPermissions).map(([module, items]) => (
                  <div key={module}>
                    <Typography.Text strong>{module}</Typography.Text>
                    <div style={{ marginTop: 8, display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 8 }}>
                      {items.map((permission) => (
                        <Checkbox key={permission.code} value={permission.code}>
                          {permission.name}（{permission.code}）
                        </Checkbox>
                      ))}
                    </div>
                  </div>
                ))}
              </Space>
            </Checkbox.Group>
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
