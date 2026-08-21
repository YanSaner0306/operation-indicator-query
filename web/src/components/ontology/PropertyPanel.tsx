import { useCallback, useEffect, useState } from 'react';
import {
  Button, Checkbox, Form, Input, InputNumber, message, Modal, Popconfirm,
  Select, Space, Table,
} from 'antd';
import { ontologyApi } from '@/api/ontology';
import { getErrorMessage } from '@/api/client';
import type {
  OntologyProperty, OntologyPropertyPayload, PropertyDataType,
} from '@/types/ontology';
import { propertyTypeText } from '@/utils/ontology';

interface Props {
  ontologyId: number;
  onChanged?: () => void;
}

export default function PropertyPanel({ ontologyId, onChanged }: Props) {
  const [items, setItems] = useState<OntologyProperty[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<OntologyProperty>();
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm<OntologyPropertyPayload>();
  const dataType = Form.useWatch('dataType', form);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setItems(await ontologyApi.properties(ontologyId));
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [ontologyId]);

  useEffect(() => { void load(); }, [load]);

  const showForm = (item?: OntologyProperty) => {
    setEditing(item);
    setOpen(true);
    form.resetFields();
    form.setFieldsValue({
      name: item?.name ?? '',
      code: item?.code ?? '',
      dataType: item?.dataType ?? 'STRING',
      length: item?.length,
      precision: item?.precision,
      scale: item?.scale,
      required: item?.required ?? false,
      unique: item?.uniqueFlag ?? false,
      defaultValue: item?.defaultValue ?? '',
      description: item?.description ?? '',
      sortOrder: item?.sortOrder ?? 0,
      status: item?.status ?? 'ENABLED',
    });
  };

  const save = async () => {
    const values = await form.validateFields();
    setSaving(true);
    try {
      if (editing) {
        await ontologyApi.updateProperty(ontologyId, editing.id, values);
      } else {
        await ontologyApi.createProperty(ontologyId, values);
      }
      message.success('属性保存成功');
      setOpen(false);
      await load();
      onChanged?.();
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (id: number) => {
    try {
      await ontologyApi.removeProperty(ontologyId, id);
      message.success('属性已删除');
      await load();
      onChanged?.();
    } catch (error) {
      message.error(getErrorMessage(error));
    }
  };

  return (
    <div>
      <div className="panel-toolbar">
        <Button type="primary" onClick={() => showForm()}>新增属性</Button>
        <Button onClick={() => void load()}>刷新</Button>
      </div>
      <Table
        rowKey="id"
        size="small"
        loading={loading}
        dataSource={items}
        pagination={false}
        columns={[
          { title: '属性名称', dataIndex: 'name', width: 140 },
          { title: '编码', dataIndex: 'code', width: 150 },
          { title: '类型', dataIndex: 'dataType', width: 90, render: (value: PropertyDataType) => propertyTypeText[value] },
          {
            title: '长度/精度', width: 100,
            render: (_, item) => item.dataType === 'STRING'
              ? item.length ?? '-'
              : item.dataType === 'DECIMAL' ? `${item.precision ?? '-'},${item.scale ?? '-'}` : '-',
          },
          { title: '是否必填', dataIndex: 'required', width: 86, render: (value: boolean) => value ? '是' : '否' },
          { title: '描述', dataIndex: 'description', ellipsis: true, render: (value?: string) => value || '-' },
          {
            title: '操作', width: 120,
            render: (_, record) => (
              <Space>
                <Button type="link" size="small" onClick={() => showForm(record)}>编辑</Button>
                <Popconfirm title="确认删除该属性？" onConfirm={() => remove(record.id)}>
                  <Button type="link" danger size="small">删除</Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />

      <Modal
        open={open}
        title={editing ? '编辑属性' : '新增属性'}
        width={680}
        confirmLoading={saving}
        onCancel={() => setOpen(false)}
        onOk={save}
        forceRender
      >
        <Form form={form} layout="vertical" preserve={false}>
          <div className="form-grid-2">
            <Form.Item name="name" label="属性名称" rules={[{ required: true }, { max: 64 }]}><Input /></Form.Item>
            <Form.Item name="code" label="属性编码" rules={[{ required: true }, { pattern: /^[A-Za-z][A-Za-z0-9_]*$/ }]}><Input /></Form.Item>
            <Form.Item name="dataType" label="数据类型" rules={[{ required: true }]}>
              <Select options={(Object.keys(propertyTypeText) as PropertyDataType[]).map((value) => ({ value, label: propertyTypeText[value] }))} />
            </Form.Item>
            <Form.Item name="status" label="状态" rules={[{ required: true }]}>
              <Select options={[{ value: 'ENABLED', label: '启用' }, { value: 'DISABLED', label: '禁用' }]} />
            </Form.Item>
            {dataType === 'STRING' && (
              <Form.Item name="length" label="字符串长度"><InputNumber min={1} max={4000} style={{ width: '100%' }} /></Form.Item>
            )}
            {dataType === 'DECIMAL' && (
              <>
                <Form.Item name="precision" label="精度"><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
                <Form.Item name="scale" label="小数位"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
              </>
            )}
            <Form.Item name="sortOrder" label="排序"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          </div>
          <Space size="large" style={{ marginBottom: 16 }}>
            <Form.Item name="required" valuePropName="checked" noStyle><Checkbox>业务必填</Checkbox></Form.Item>
            <Form.Item name="unique" valuePropName="checked" noStyle><Checkbox>业务唯一标识</Checkbox></Form.Item>
          </Space>
          <Form.Item name="defaultValue" label="默认值"><Input /></Form.Item>
          <Form.Item name="description" label="说明" rules={[{ max: 500 }]}><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
