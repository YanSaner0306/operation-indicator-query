import { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Form, Input, message, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import { ontologyApi } from '@/api/ontology';
import { getErrorMessage } from '@/api/client';
import type {
  Ontology, OntologyProperty, OntologyRelation, OntologyRelationPayload, RelationCardinality,
} from '@/types/ontology';
import { statusText } from '@/utils/ontology';

interface Props {
  ontologyId: number;
  ontologies: Ontology[];
  onChanged?: () => void;
  onViewGraph?: () => void;
}

const cardinalityText: Record<RelationCardinality, string> = {
  ONE_TO_ONE: '一对一',
  ONE_TO_MANY: '一对多',
  MANY_TO_ONE: '多对一',
  MANY_TO_MANY: '多对多',
};

export default function RelationPanel({ ontologyId, ontologies, onChanged, onViewGraph }: Props) {
  const [items, setItems] = useState<OntologyRelation[]>([]);
  const [sourceProperties, setSourceProperties] = useState<OntologyProperty[]>([]);
  const [targetProperties, setTargetProperties] = useState<OntologyProperty[]>([]);
  const [editing, setEditing] = useState<OntologyRelation>();
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm<OntologyRelationPayload>();
  const targetOntologyId = Form.useWatch('targetOntologyId', form);

  const ontologyMap = useMemo(() => new Map(ontologies.map((item) => [item.id, item])), [ontologies]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [relations, properties] = await Promise.all([
        ontologyApi.relations(ontologyId),
        ontologyApi.properties(ontologyId),
      ]);
      setItems(relations);
      setSourceProperties(properties);
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [ontologyId]);

  useEffect(() => { void load(); }, [load]);

  useEffect(() => {
    if (!open || !targetOntologyId) {
      setTargetProperties([]);
      return;
    }
    ontologyApi.properties(targetOntologyId)
      .then(setTargetProperties)
      .catch((error) => message.error(getErrorMessage(error)));
  }, [open, targetOntologyId]);

  const showForm = (item?: OntologyRelation) => {
    setEditing(item);
    setOpen(true);
    form.resetFields();
    form.setFieldsValue({
      name: item?.name ?? '',
      code: item?.code ?? '',
      targetOntologyId: item?.targetOntologyId,
      cardinality: item?.cardinality ?? 'MANY_TO_ONE',
      sourcePropertyId: item?.sourcePropertyId,
      targetPropertyId: item?.targetPropertyId,
      description: item?.description ?? '',
      status: item?.status ?? 'ENABLED',
      version: item?.version,
    });
  };

  const save = async () => {
    const values = await form.validateFields();
    setSaving(true);
    try {
      if (editing) {
        await ontologyApi.updateRelation(ontologyId, editing.id, { ...values, version: editing.version });
      } else {
        await ontologyApi.createRelation(ontologyId, values);
      }
      message.success('关系保存成功');
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
      await ontologyApi.removeRelation(ontologyId, id);
      message.success('关系已删除');
      await load();
      onChanged?.();
    } catch (error) {
      message.error(getErrorMessage(error));
    }
  };

  const propertyName = (properties: OntologyProperty[], id?: number) =>
    id ? properties.find((item) => item.id === id)?.name ?? `#${id}` : '-';

  return (
    <div>
      <div className="panel-toolbar">
        <Button type="primary" onClick={() => showForm()}>新增关系</Button>
        <Button onClick={() => void load()}>刷新</Button>
        {onViewGraph && <Button onClick={onViewGraph}>查看关系图谱</Button>}
      </div>
      <Table
        rowKey="id"
        size="small"
        loading={loading}
        dataSource={items}
        pagination={false}
        scroll={{ x: 920 }}
        columns={[
          { title: '关系名称', dataIndex: 'name', width: 150 },
          { title: '编码', dataIndex: 'code', width: 160 },
          { title: '目标本体', dataIndex: 'targetOntologyId', width: 140, render: (id: number) => ontologyMap.get(id)?.name ?? `#${id}` },
          { title: '基数', dataIndex: 'cardinality', width: 90, render: (value: RelationCardinality) => cardinalityText[value] },
          { title: '起点属性', dataIndex: 'sourcePropertyId', width: 130, render: (id?: number) => propertyName(sourceProperties, id) },
          { title: '状态', dataIndex: 'status', width: 80, render: (value) => <Tag color={value === 'ENABLED' ? 'green' : 'default'}>{statusText[value as 'ENABLED' | 'DISABLED']}</Tag> },
          {
            title: '操作', width: 130, fixed: 'right', render: (_, record) => (
              <Space>
                <Button type="link" size="small" onClick={() => showForm(record)}>编辑</Button>
                <Popconfirm title="确认删除该关系？" onConfirm={() => remove(record.id)}>
                  <Button type="link" danger size="small">删除</Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />

      <Modal
        open={open}
        title={editing ? '编辑本体关系' : '新增本体关系'}
        width={700}
        confirmLoading={saving}
        onCancel={() => setOpen(false)}
        onOk={save}
        forceRender
      >
        <Form form={form} layout="vertical" preserve={false}>
          <div className="form-grid-2">
            <Form.Item name="name" label="关系名称" rules={[{ required: true }, { max: 64 }]}><Input /></Form.Item>
            <Form.Item name="code" label="关系编码" rules={[{ required: true }, { pattern: /^[A-Za-z][A-Za-z0-9_]*$/ }]}><Input /></Form.Item>
            <Form.Item name="targetOntologyId" label="目标本体" rules={[{ required: true }]}>
              <Select showSearch optionFilterProp="label" options={ontologies.map((item) => ({ value: item.id, label: item.name }))} />
            </Form.Item>
            <Form.Item name="cardinality" label="关系基数" rules={[{ required: true }]}>
              <Select options={(Object.keys(cardinalityText) as RelationCardinality[]).map((value) => ({ value, label: cardinalityText[value] }))} />
            </Form.Item>
            <Form.Item name="sourcePropertyId" label="起点属性">
              <Select allowClear options={sourceProperties.map((item) => ({ value: item.id, label: `${item.name} (${item.dataType})` }))} />
            </Form.Item>
            <Form.Item name="targetPropertyId" label="目标属性">
              <Select allowClear options={targetProperties.map((item) => ({ value: item.id, label: `${item.name} (${item.dataType})` }))} />
            </Form.Item>
            <Form.Item name="status" label="状态" rules={[{ required: true }]}>
              <Select options={[{ value: 'ENABLED', label: '启用' }, { value: 'DISABLED', label: '禁用' }]} />
            </Form.Item>
          </div>
          <Form.Item name="description" label="说明" rules={[{ max: 500 }]}><Input.TextArea rows={3} /></Form.Item>
          <Form.Item name="version" hidden><Input /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
