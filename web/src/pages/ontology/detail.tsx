import { useCallback, useEffect, useState } from 'react';
import {
  Button, Card, Descriptions, Form, Input, message, Select, Space, Spin,
  Tabs, Tag, Typography,
} from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { ontologyApi } from '@/api/ontology';
import { getErrorMessage } from '@/api/client';
import { useDomainTree } from '@/hooks/useDomainTree';
import type { Ontology, OntologyPayload } from '@/types/ontology';
import { formatDateTime, statusText } from '@/utils/ontology';
import PropertyPanel from '@/components/ontology/PropertyPanel';
import RelationPanel from '@/components/ontology/RelationPanel';
import OntologyRulePanel from '@/components/ontology/OntologyRulePanel';

export default function OntologyDetailPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const ontologyId = Number(id);
  const domains = useDomainTree();
  const [ontology, setOntology] = useState<Ontology>();
  const [allOntologies, setAllOntologies] = useState<Ontology[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm<OntologyPayload>();

  const load = useCallback(async () => {
    if (!Number.isFinite(ontologyId)) return;
    try {
      const [detail, all] = await Promise.all([
        ontologyApi.get(ontologyId),
        ontologyApi.page({ page: 0, size: 100 }),
      ]);
      setOntology(detail);
      setAllOntologies(all.items);
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [ontologyId]);

  useEffect(() => { void load(); }, [load]);
  useEffect(() => {
    if (!ontology) return;
    form.setFieldsValue(toFormValues(ontology));
  }, [form, ontology]);

  const save = async () => {
    if (!ontology) return;
    const values = await form.validateFields();
    setSaving(true);
    try {
      const updated = await ontologyApi.update(ontology.id, { ...values, version: ontology.version });
      setOntology(updated);
      form.setFieldsValue({ ...updated, version: updated.version });
      message.success('本体保存成功');
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <Card><Spin /></Card>;
  }
  if (!ontology) {
    return <Card><Typography.Text type="secondary">本体不存在或已被删除。</Typography.Text></Card>;
  }

  return (
    <div className="ontology-detail-page">
      <div className="detail-page-header">
        <Button type="text" onClick={() => navigate('/ontology')}>
          ‹ 返回本体列表
        </Button>
        <div className="detail-page-title">
          <Typography.Title level={4}>{ontology.name}</Typography.Title>
          <Typography.Text type="secondary">{ontology.code}</Typography.Text>
          <Tag color={ontology.status === 'ENABLED' ? 'green' : 'default'}>{statusText[ontology.status]}</Tag>
        </div>
      </div>

      <Card>
        <Tabs
          items={[
            {
              key: 'base',
              label: '基本信息',
              children: (
                <Form form={form} layout="vertical" preserve={false}>
                  <div className="form-grid-2 ontology-base-form">
                    <Form.Item name="name" label="本体名称" rules={[{ required: true }, { max: 64 }]}>
                      <Input />
                    </Form.Item>
                    <Form.Item name="code" label="编码" rules={[{ required: true }, { max: 64 }, { pattern: /^[A-Za-z][A-Za-z0-9_]*$/ }]}>
                      <Input />
                    </Form.Item>
                    <Form.Item name="domainIds" label="所属领域">
                      <Select
                        mode="multiple"
                        allowClear
                        placeholder="本体可以暂时不归类"
                        options={domains.flatDomains.map((item) => ({
                          value: item.id,
                          label: `${'　'.repeat(item.level)}${item.name}`,
                          disabled: item.status === 'DISABLED' && !ontology.domainIds.includes(item.id),
                        }))}
                      />
                    </Form.Item>
                    <Form.Item name="status" label="状态" rules={[{ required: true }]}>
                      <Select options={[{ value: 'ENABLED', label: '启用' }, { value: 'DISABLED', label: '禁用' }]} />
                    </Form.Item>
                    <Form.Item name="description" label="描述" className="form-span-2" rules={[{ max: 500 }]}>
                      <Input.TextArea rows={4} />
                    </Form.Item>
                  </div>
                  <Descriptions size="small" column={2} className="detail-meta">
                    <Descriptions.Item label="创建时间">{formatDateTime(ontology.createdAt)}</Descriptions.Item>
                    <Descriptions.Item label="更新时间">{formatDateTime(ontology.updatedAt)}</Descriptions.Item>
                  </Descriptions>
                  <div className="detail-form-actions">
                    <Space>
                      <Button onClick={() => form.setFieldsValue(toFormValues(ontology))}>取消</Button>
                      <Button type="primary" loading={saving} onClick={() => void save()}>保存</Button>
                    </Space>
                  </div>
                </Form>
              ),
            },
            {
              key: 'properties',
              label: '属性',
              children: <PropertyPanel ontologyId={ontology.id} onChanged={() => void load()} />,
            },
            {
              key: 'relations',
              label: '关联关系',
              children: (
                <RelationPanel
                  ontologyId={ontology.id}
                  ontologies={allOntologies}
                  onChanged={() => void load()}
                  onViewGraph={() => navigate(`/ontology/${ontology.id}/graph`)}
                />
              ),
            },
            {
              key: 'rules',
              label: '规则',
              children: <OntologyRulePanel ontologyId={ontology.id} />,
            },
          ]}
        />
      </Card>
    </div>
  );
}

function toFormValues(ontology: Ontology): OntologyPayload {
  return {
    name: ontology.name,
    code: ontology.code,
    description: ontology.description ?? '',
    status: ontology.status,
    domainIds: ontology.domainIds,
    version: ontology.version,
  };
}
