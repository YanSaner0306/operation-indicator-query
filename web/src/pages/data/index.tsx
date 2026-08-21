/**
 * 模块6-8：数据源管理与安全预览页面。
 * 功能：完成配置增改删、启停、连接测试、表列浏览和最多100行的脱敏预览。
 * 技术栈：React、Ant Design Table/Modal/Drawer、RBAC按钮组件和Axios。
 */
import { useEffect, useState } from 'react';
import { Button, Card, Drawer, Form, Input, InputNumber, message, Modal, Select, Space, Switch, Table, Tag } from 'antd';
import { dataApi } from '@/api/data';
import { getErrorMessage } from '@/api/client';
import Permission from '@/auth/Permission';
import type { ColumnMetadata, DataSourceConfig, SaveDataSourcePayload, TableMetadata } from '@/types/data-source';

export default function DataPage() {
  const [items, setItems] = useState<DataSourceConfig[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<DataSourceConfig | null>();
  const [form] = Form.useForm<SaveDataSourcePayload>();
  const [exploring, setExploring] = useState<DataSourceConfig | null>(null);
  const [tables, setTables] = useState<TableMetadata[]>([]);
  const [columns, setColumns] = useState<ColumnMetadata[]>([]);
  const [previewRows, setPreviewRows] = useState<Record<string, unknown>[]>([]);

  const load = async () => {
    setLoading(true);
    try { setItems((await dataApi.page({ page: 1, size: 100 })).items); }
    catch (error) { message.error(getErrorMessage(error)); }
    finally { setLoading(false); }
  };
  useEffect(() => { void load(); }, []);

  const openForm = (item?: DataSourceConfig) => {
    setEditing(item ?? null);
    form.setFieldsValue(item ? { ...item, password: '' } : { dbType: 'MYSQL', port: 3306 });
  };
  const save = async () => {
    try {
      const values = await form.validateFields();
      if (editing) await dataApi.update(editing.id, { ...values, version: editing.version });
      else await dataApi.create(values);
      message.success('保存成功'); setEditing(undefined); form.resetFields(); await load();
    } catch (error) { if (error instanceof Error) message.error(getErrorMessage(error)); }
  };
  const explore = async (item: DataSourceConfig) => {
    setExploring(item); setColumns([]); setPreviewRows([]);
    try { setTables(await dataApi.tables(item.id)); } catch (error) { message.error(getErrorMessage(error)); }
  };
  const selectTable = async (table: string) => {
    if (!exploring) return;
    try {
      const metadata = await dataApi.columns(exploring.id, table); setColumns(metadata);
      setPreviewRows((await dataApi.preview(exploring.id, table, metadata.map((column) => column.name), 20)).rows);
    } catch (error) { message.error(getErrorMessage(error)); }
  };

  return (
    <Card title="数据源管理" extra={<Permission code="DATASOURCE_MANAGE"><Button type="primary" onClick={() => openForm()}>新建数据源</Button></Permission>}>
      <Table rowKey="id" loading={loading} dataSource={items} pagination={false} columns={[
        { title: '名称', dataIndex: 'name' }, { title: '类型', dataIndex: 'dbType' },
        { title: '连接地址', render: (_, row) => `${row.host}:${row.port}/${row.databaseName}` },
        { title: '状态', render: (_, row) => <Tag color={row.status === 'ENABLED' ? 'green' : 'default'}>{row.status}</Tag> },
        { title: '测试', render: (_, row) => <Tag color={row.lastTestStatus === 'SUCCESS' ? 'green' : row.lastTestStatus === 'FAILED' ? 'red' : 'default'}>{row.lastTestStatus}</Tag> },
        { title: '操作', render: (_, row) => <Space wrap>
          <Button size="small" onClick={() => explore(row)} disabled={row.status !== 'ENABLED'}>浏览数据</Button>
          <Permission code="DATASOURCE_MANAGE"><Button size="small" onClick={() => openForm(row)}>编辑</Button></Permission>
          <Permission code="DATASOURCE_MANAGE"><Button size="small" onClick={async () => { try { const result=await dataApi.test(row.id); message.success(`${result.message}（${result.latencyMs}ms）`); await load(); } catch(error) { message.error(getErrorMessage(error)); await load(); } }}>测试</Button></Permission>
          <Permission code="DATASOURCE_MANAGE"><Switch size="small" checked={row.status === 'ENABLED'} onChange={async (checked) => { try { await dataApi.setStatus(row, checked ? 'ENABLED' : 'DISABLED'); await load(); } catch(error) { message.error(getErrorMessage(error)); } }} /></Permission>
          <Permission code="DATASOURCE_MANAGE"><Button size="small" danger onClick={() => Modal.confirm({ title: '确认删除该数据源？', onOk: async () => { await dataApi.remove(row); await load(); } })}>删除</Button></Permission>
        </Space> },
      ]} />

      <Modal title={editing ? '编辑数据源' : '新建数据源'} open={editing !== undefined} onCancel={() => setEditing(undefined)} onOk={save} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="dbType" label="数据库类型" rules={[{ required: true }]}><Select options={[{ value: 'MYSQL', label: 'MySQL' }]} /></Form.Item>
          <Space align="start"><Form.Item name="host" label="主机" rules={[{ required: true }]}><Input /></Form.Item><Form.Item name="port" label="端口" rules={[{ required: true }]}><InputNumber min={1} max={65535} /></Form.Item></Space>
          <Form.Item name="databaseName" label="数据库名" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="username" label="用户名" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="password" label={editing ? '密码（留空表示不修改）' : '密码'} rules={editing ? [] : [{ required: true }]}><Input.Password autoComplete="new-password" /></Form.Item>
        </Form>
      </Modal>

      <Drawer title={`浏览数据：${exploring?.name ?? ''}`} open={!!exploring} onClose={() => setExploring(null)} width="80%">
        <Space align="start" style={{ width: '100%' }}>
          <Table<TableMetadata> rowKey="name" size="small" pagination={false} dataSource={tables} columns={[{ title: '表', dataIndex: 'name' }]} onRow={(row) => ({ onClick: () => selectTable(row.name) })} />
          <Table rowKey="name" size="small" pagination={false} dataSource={columns} columns={[{ title: '列', dataIndex: 'name' }, { title: '类型', dataIndex: 'typeName' }, { title: '主键', render: (_, row) => row.primaryKey ? '是' : '否' }]} />
        </Space>
        {columns.length > 0 && <Table style={{ marginTop: 16 }} size="small" dataSource={previewRows} pagination={false} rowKey={(_, index) => String(index)} columns={columns.map((column) => ({ title: column.name, dataIndex: column.name }))} />}
      </Drawer>
    </Card>
  );
}
