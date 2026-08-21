import { useCallback, useEffect, useState } from 'react';
import { Button, message, Space, Table, Tag } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ruleApi } from '@/api/rule';
import { getErrorMessage } from '@/api/client';
import type { Rule } from '@/types/rule';
import { operatorText } from '@/utils/ontology';

export default function OntologyRulePanel({ ontologyId }: { ontologyId: number }) {
  const navigate = useNavigate();
  const [items, setItems] = useState<Rule[]>([]);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = await ruleApi.page({ ontologyId, page: 0, size: 100 });
      setItems(result.items);
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [ontologyId]);

  useEffect(() => { void load(); }, [load]);

  return (
    <div>
      <div className="panel-toolbar">
        <Button type="primary" onClick={() => navigate(`/rules/new?ontologyId=${ontologyId}`)}>新增规则</Button>
        <Button onClick={() => navigate(`/rules?ontologyId=${ontologyId}`)}>进入规则管理</Button>
      </div>
      <Table
        rowKey="id"
        size="small"
        loading={loading}
        dataSource={items}
        pagination={false}
        columns={[
          { title: '名称', dataIndex: 'name' },
          { title: '编码', dataIndex: 'code' },
          { title: '当前版本', dataIndex: 'currentVersionNo', width: 90, render: (value: number) => `v${value}` },
          {
            title: '条件',
            render: (_, item) => `${item.condition.propertyName} ${operatorText[item.condition.operator]} ${item.condition.compareValue ?? ''}`,
          },
          { title: '状态', dataIndex: 'enabled', width: 80, render: (value: boolean) => <Tag color={value ? 'green' : 'default'}>{value ? '启用' : '禁用'}</Tag> },
          {
            title: '操作', width: 130,
            render: (_, item) => (
              <Space size={0}>
                <Button type="link" size="small" onClick={() => navigate(`/rules/${item.id}/edit`)}>编辑</Button>
                <Button type="link" size="small" onClick={() => navigate(`/rules/${item.id}/test`)}>测试</Button>
              </Space>
            ),
          },
        ]}
      />
    </div>
  );
}
