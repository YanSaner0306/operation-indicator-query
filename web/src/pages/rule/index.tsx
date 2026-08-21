import { useCallback, useEffect, useState } from 'react';
import {
  Button, Card, Input, message, Popconfirm, Select, Space, Table, Tag, Typography,
} from 'antd';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ontologyApi } from '@/api/ontology';
import { ruleApi } from '@/api/rule';
import { getErrorMessage } from '@/api/client';
import RuleVersionDrawer from '@/components/rule/RuleVersionDrawer';
import type { Ontology } from '@/types/ontology';
import type { Rule } from '@/types/rule';
import { formatDateTime, operatorText } from '@/utils/ontology';

export default function RulePage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const queryOntologyId = searchParams.get('ontologyId');
  const initialOntologyId = queryOntologyId ? Number(queryOntologyId) : undefined;
  const [items, setItems] = useState<Rule[]>([]);
  const [ontologies, setOntologies] = useState<Ontology[]>([]);
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [enabled, setEnabled] = useState<boolean>();
  const [ontologyId, setOntologyId] = useState<number | undefined>(initialOntologyId);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [versionsOf, setVersionsOf] = useState<Rule>();

  const loadOntologies = useCallback(async () => {
    try {
      const result = await ontologyApi.page({ page: 0, size: 100 });
      setOntologies(result.items);
    } catch (error) {
      message.error(getErrorMessage(error));
    }
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = await ruleApi.page({
        keyword: keyword || undefined,
        ontologyId,
        enabled,
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
  }, [enabled, keyword, ontologyId, page, size]);

  useEffect(() => { void load(); }, [load]);
  useEffect(() => { void loadOntologies(); }, [loadOntologies]);
  const refresh = async () => {
    await Promise.all([load(), loadOntologies()]);
  };

  const toggle = async (item: Rule, checked: boolean) => {
    try {
      await ruleApi.updateEnabled(item.id, checked, item.version);
      message.success(checked ? '规则已启用' : '规则已禁用');
      await load();
    } catch (error) {
      message.error(getErrorMessage(error));
    }
  };

  const remove = async (item: Rule) => {
    try {
      await ruleApi.remove(item.id);
      message.success('规则已删除');
      await load();
    } catch (error) {
      message.error(getErrorMessage(error));
    }
  };

  return (
    <div>
      <Typography.Title level={3}>规则管理</Typography.Title>
      <Typography.Paragraph type="secondary">
        每条规则只包含一个本体属性条件；编辑会生成新版本，手工测试不访问真实业务数据库。
      </Typography.Paragraph>
      <Card title="规则列表">
        <div className="page-toolbar rule-list-toolbar">
          <Space wrap size={8}>
            <Button type="primary" onClick={() => navigate(initialOntologyId ? `/rules/new?ontologyId=${initialOntologyId}` : '/rules/new')}>＋ 新增规则</Button>
            <Input.Search
              allowClear
              placeholder="搜索规则名称或编码"
              style={{ width: 220 }}
              onSearch={(value) => { setKeyword(value.trim()); setPage(0); }}
            />
            <Select
              allowClear
              showSearch
              optionFilterProp="label"
              placeholder="所属本体"
              style={{ width: 160 }}
              value={ontologyId}
              onChange={(value) => { setOntologyId(value); setPage(0); }}
              options={ontologies.map((item) => ({ value: item.id, label: item.name, disabled: item.status === 'DISABLED' }))}
            />
            <Select
              allowClear
              placeholder="状态"
              style={{ width: 96 }}
              value={enabled}
              onChange={(value) => { setEnabled(value); setPage(0); }}
              options={[{ value: true, label: '启用' }, { value: false, label: '禁用' }]}
            />
            <Button onClick={() => void refresh()}>刷新</Button>
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
            { title: '规则名称', dataIndex: 'name', width: 130, ellipsis: true },
            { title: '所属本体', dataIndex: 'ontologyName', width: 110, ellipsis: true },
            { title: '属性', width: 100, ellipsis: true, render: (_, item) => item.condition.propertyName },
            { title: '条件', width: 130, ellipsis: true, render: (_, item) => `${operatorText[item.condition.operator]} ${item.condition.compareValue ?? ''}` },
            { title: '当前版本', dataIndex: 'currentVersionNo', width: 70, align: 'center', render: (value: number) => `v${value}` },
            { title: '状态', dataIndex: 'enabled', width: 70, render: (value: boolean) => <Tag color={value ? 'green' : 'default'}>{value ? '启用' : '禁用'}</Tag> },
            { title: '更新时间', dataIndex: 'updatedAt', width: 130, ellipsis: true, render: formatDateTime },
            {
              title: '操作', width: 220,
              render: (_, item) => (
                <Space size={0}>
                  <Button type="link" size="small" onClick={() => navigate(`/rules/${item.id}/edit`)}>编辑</Button>
                  <Button type="link" size="small" onClick={() => setVersionsOf(item)}>版本</Button>
                  <Button type="link" size="small" onClick={() => void toggle(item, !item.enabled)}>{item.enabled ? '禁用' : '启用'}</Button>
                  <Button type="link" size="small" onClick={() => navigate(`/rules/${item.id}/test`)}>测试</Button>
                  <Popconfirm title="确认删除该规则？" description="仅禁用且未被引用的规则可删除。" onConfirm={() => remove(item)}>
                    <Button type="link" danger size="small">删除</Button>
                  </Popconfirm>
                </Space>
              ),
            },
          ]}
        />
      </Card>

      <RuleVersionDrawer
        open={Boolean(versionsOf)}
        rule={versionsOf}
        onClose={() => setVersionsOf(undefined)}
        onChanged={() => void load()}
      />
    </div>
  );
}
