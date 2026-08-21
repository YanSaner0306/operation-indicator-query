import { useCallback, useEffect, useState } from 'react';
import {
  Button, Card, Col, Input, message, Popconfirm, Row, Select, Space,
  Table, Tag, Typography,
} from 'antd';
import { useNavigate } from 'react-router-dom';
import DomainTreePanel from '@/components/domain/DomainTreePanel';
import OntologyFormModal from '@/components/ontology/OntologyFormModal';
import { ontologyApi } from '@/api/ontology';
import { getErrorMessage } from '@/api/client';
import { useDomainTree } from '@/hooks/useDomainTree';
import type { ConfigStatus, Ontology, OntologyPayload } from '@/types/ontology';
import { formatDateTime, statusText } from '@/utils/ontology';

const ALL_SCOPE = 'all';
const UNCLASSIFIED_SCOPE = 'unclassified';

export default function OntologyPage() {
  const navigate = useNavigate();
  const domains = useDomainTree();
  const [items, setItems] = useState<Ontology[]>([]);
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<ConfigStatus>();
  const [unclassified, setUnclassified] = useState(false);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = await ontologyApi.page({
        domainId: unclassified ? undefined : domains.selectedId,
        unclassified: unclassified || undefined,
        keyword: keyword || undefined,
        status,
        page,
        size,
      });
      setItems(result.items);
      setTotal(result.total);
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [domains.selectedId, keyword, page, size, status, unclassified]);

  useEffect(() => { void load(); }, [load]);

  const save = async (payload: OntologyPayload) => {
    setSaving(true);
    try {
      await ontologyApi.create(payload);
      message.success('本体创建成功');
      setFormOpen(false);
      await load();
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (item: Ontology) => {
    try {
      await ontologyApi.remove(item.id);
      message.success('本体已删除');
      await load();
    } catch (error) {
      message.error(getErrorMessage(error));
    }
  };

  const toggleStatus = async (item: Ontology) => {
    const nextStatus: ConfigStatus = item.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
    try {
      await ontologyApi.updateStatus(item.id, nextStatus, item.version);
      message.success(nextStatus === 'ENABLED' ? '本体已启用' : '本体已禁用');
      await load();
    } catch (error) {
      message.error(getErrorMessage(error));
    }
  };

  const scopeValue = unclassified
    ? UNCLASSIFIED_SCOPE
    : domains.selectedId ? String(domains.selectedId) : ALL_SCOPE;

  const changeScope = (value: string) => {
    if (value === UNCLASSIFIED_SCOPE) {
      setUnclassified(true);
      domains.setSelectedId(undefined);
    } else if (value === ALL_SCOPE) {
      setUnclassified(false);
      domains.setSelectedId(undefined);
    } else {
      setUnclassified(false);
      domains.setSelectedId(Number(value));
    }
    setPage(0);
  };

  return (
    <div>
      <Typography.Title level={3}>本体管理</Typography.Title>
      <Typography.Paragraph type="secondary">
        左侧维护业务领域，右侧查看和维护当前范围内的本体。
      </Typography.Paragraph>
      <Row gutter={16} align="top">
        <Col xs={24} xl={7}>
          <DomainTreePanel
            tree={domains.tree}
            flatDomains={domains.flatDomains}
            loading={domains.loading}
            selectedId={domains.selectedId}
            onSelect={(id) => {
              setUnclassified(false);
              domains.setSelectedId(id);
              setPage(0);
            }}
            onChanged={domains.refresh}
            unclassifiedSelected={unclassified}
            onSelectUnclassified={() => {
              setUnclassified(true);
              domains.setSelectedId(undefined);
              setPage(0);
            }}
          />
        </Col>
        <Col xs={24} xl={17}>
          <Card title="本体列表" className="ontology-list-card">
            <div className="page-toolbar ontology-list-toolbar">
              <Space wrap size={8}>
                <Select
                  value={scopeValue}
                  style={{ width: 150 }}
                  onChange={changeScope}
                  options={[
                    { value: ALL_SCOPE, label: '全部本体' },
                    { value: UNCLASSIFIED_SCOPE, label: '未归类本体' },
                    ...domains.flatDomains.map((item) => ({
                      value: String(item.id),
                      label: `${'　'.repeat(item.level)}${item.name}`,
                    })),
                  ]}
                />
                <Input.Search
                  allowClear
                  placeholder="输入本体名称或编码搜索"
                  style={{ width: 210 }}
                  onSearch={(value) => { setKeyword(value.trim()); setPage(0); }}
                />
                <Select
                  allowClear
                  placeholder="状态"
                  style={{ width: 96 }}
                  value={status}
                  onChange={(value) => { setStatus(value); setPage(0); }}
                  options={[{ value: 'ENABLED', label: '启用' }, { value: 'DISABLED', label: '禁用' }]}
                />
                <Button onClick={() => void load()}>刷新</Button>
                <Button type="primary" onClick={() => setFormOpen(true)}>＋ 新增本体</Button>
              </Space>
            </div>
            <Table
              rowKey="id"
              size="small"
              tableLayout="fixed"
              loading={loading}
              dataSource={items}
              pagination={{
                current: page + 1,
                pageSize: size,
                total,
                showSizeChanger: true,
                pageSizeOptions: [10, 20, 50],
                showTotal: (value) => `共 ${value} 条`,
                onChange: (nextPage, nextSize) => { setPage(nextPage - 1); setSize(nextSize); },
              }}
              columns={[
                {
                  title: '本体名称', dataIndex: 'name', width: 100,
                  render: (value: string, item: Ontology) => (
                    <Button type="link" className="table-name-link" onClick={() => navigate(`/ontology/${item.id}`)}>
                      {value}
                    </Button>
                  ),
                },
                { title: '编码', dataIndex: 'code', width: 88, ellipsis: true },
                { title: '描述', dataIndex: 'description', width: 70, ellipsis: true, render: (value?: string) => value || '-' },
                { title: '属性数', dataIndex: 'propertyCount', width: 50, align: 'center' },
                { title: '关系数', dataIndex: 'relationCount', width: 50, align: 'center' },
                {
                  title: '状态', dataIndex: 'status', width: 60,
                  render: (value: ConfigStatus) => <Tag color={value === 'ENABLED' ? 'green' : 'default'}>{statusText[value]}</Tag>,
                },
                { title: '更新时间', dataIndex: 'updatedAt', width: 115, ellipsis: true, render: formatDateTime },
                {
                  title: '操作', width: 135,
                  render: (_, item: Ontology) => (
                    <Space size={0}>
                      <Button type="link" size="small" onClick={() => navigate(`/ontology/${item.id}`)}>编辑</Button>
                      <Popconfirm
                        title={`确认${item.status === 'ENABLED' ? '禁用' : '启用'}该本体？`}
                        onConfirm={() => toggleStatus(item)}
                      >
                        <Button type="link" size="small">{item.status === 'ENABLED' ? '禁用' : '启用'}</Button>
                      </Popconfirm>
                      <Popconfirm
                        title="确认删除该本体？"
                        description="存在属性、关系、规则或 Binding 引用时无法删除。"
                        onConfirm={() => remove(item)}
                      >
                        <Button type="link" danger size="small">删除</Button>
                      </Popconfirm>
                    </Space>
                  ),
                },
              ]}
            />
          </Card>
        </Col>
      </Row>

      <OntologyFormModal
        open={formOpen}
        domains={domains.flatDomains}
        initialDomainId={unclassified ? undefined : domains.selectedId}
        confirmLoading={saving}
        onCancel={() => setFormOpen(false)}
        onSubmit={save}
      />
    </div>
  );
}
