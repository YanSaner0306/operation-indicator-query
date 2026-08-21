import { useMemo, useState } from 'react';
import { Button, Card, Empty, message, Popconfirm, Space, Spin, Tag, Tree, Typography } from 'antd';
import { domainApi } from '@/api/domain';
import { getErrorMessage } from '@/api/client';
import type { DomainNode, DomainPayload, ParentDomainPayload } from '@/types/ontology';
import type { FlatDomain } from '@/utils/ontology';
import DomainFormModal from './DomainFormModal';
import ParentDomainFormModal from './ParentDomainFormModal';

interface Props {
  tree: DomainNode[];
  flatDomains: FlatDomain[];
  loading: boolean;
  selectedId?: number;
  onSelect: (id?: number) => void;
  onChanged: () => Promise<void>;
  showAllOption?: boolean;
  showUnclassifiedOption?: boolean;
  unclassifiedSelected?: boolean;
  onSelectUnclassified?: () => void;
}

type EditMode = 'domain' | 'parent' | 'edit' | undefined;

export default function DomainTreePanel({
  tree,
  flatDomains,
  loading,
  selectedId,
  onSelect,
  onChanged,
  showAllOption = false,
  showUnclassifiedOption = false,
  unclassifiedSelected = false,
  onSelectUnclassified,
}: Props) {
  const [mode, setMode] = useState<EditMode>();
  const [saving, setSaving] = useState(false);
  const selected = flatDomains.find((item) => item.id === selectedId);
  const parentDomains = flatDomains.filter((item) => (item.children?.length ?? 0) > 0);
  const selectedParentId = selected && (selected.children?.length ?? 0) > 0 ? selected.id : undefined;
  const defaultParentId = parentDomains.length === 1 ? parentDomains[0].id : undefined;
  const childParentId = selectedParentId ?? defaultParentId;

  const treeData = useMemo(() => tree.map(toTreeNode), [tree]);

  const save = async (values: DomainPayload) => {
    setSaving(true);
    try {
      if (mode === 'edit' && selected) {
        await domainApi.update(selected.id, { ...values, version: selected.version });
      } else {
        await domainApi.create(values);
      }
      message.success('领域保存成功');
      setMode(undefined);
      await onChanged();
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  const saveParent = async (values: ParentDomainPayload) => {
    setSaving(true);
    try {
      const parent = await domainApi.createParent(values);
      message.success('父领域创建并归组成功');
      setMode(undefined);
      await onChanged();
      onSelect(parent.id);
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  const remove = async () => {
    if (!selected) return;
    try {
      await domainApi.remove(selected.id);
      message.success('领域已删除');
      onSelect(undefined);
      await onChanged();
    } catch (error) {
      message.error(getErrorMessage(error));
    }
  };

  return (
    <Card
      size="small"
      title="领域树"
      extra={<Button type="link" size="small" onClick={() => void onChanged()}>刷新</Button>}
      className="domain-tree-card"
    >
      <Space wrap style={{ marginBottom: 12 }}>
        <Button size="small" type="primary" onClick={() => setMode('domain')}>新增领域</Button>
        <Button size="small" onClick={() => setMode('parent')}>新增父领域</Button>
        <Button size="small" disabled={!selected} onClick={() => setMode('edit')}>编辑</Button>
        <Popconfirm
          title="确认删除该领域？"
          description="存在子领域或本体关联时无法删除。"
          disabled={!selected}
          onConfirm={remove}
        >
          <Button size="small" danger disabled={!selected}>删除</Button>
        </Popconfirm>
      </Space>

      {showAllOption && (
        <Button
          type={!selectedId && !unclassifiedSelected ? 'primary' : 'text'}
          block
          style={{ textAlign: 'left', marginBottom: 6 }}
          onClick={() => onSelect(undefined)}
        >
          全部本体
        </Button>
      )}

      {showUnclassifiedOption && (
        <Button
          type={unclassifiedSelected ? 'primary' : 'text'}
          block
          style={{ textAlign: 'left', marginBottom: 6 }}
          onClick={onSelectUnclassified}
        >
          未归类本体
        </Button>
      )}

      <Spin spinning={loading}>
        {treeData.length ? (
          <Tree
            blockNode
            defaultExpandAll
            selectedKeys={!unclassifiedSelected && selectedId ? [selectedId] : []}
            treeData={treeData}
            onSelect={(keys) => onSelect(keys.length ? Number(keys[0]) : undefined)}
            titleRender={(node) => {
              const domain = node.source;
              return (
                <span className="tree-node-title">
                  <span>{domain.name}</span>
                  {domain.status === 'DISABLED' && <Tag color="default">禁用</Tag>}
                </span>
              );
            }}
          />
        ) : (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无领域" />
        )}
      </Spin>

      {selected && (
        <div className="domain-summary">
          <Typography.Text strong>{selected.code}</Typography.Text>
          <Typography.Paragraph type="secondary" ellipsis={{ rows: 2 }} style={{ marginBottom: 0 }}>
            {selected.description || '暂无说明'}
          </Typography.Paragraph>
        </div>
      )}

      <DomainFormModal
        open={mode === 'domain' || mode === 'edit'}
        domain={mode === 'edit' ? selected : undefined}
        parentId={mode === 'domain' ? childParentId : undefined}
        domains={parentDomains}
        allowParentSelection={mode !== 'edit' || (selected?.children?.length ?? 0) === 0}
        confirmLoading={saving}
        onCancel={() => setMode(undefined)}
        onSubmit={save}
      />
      <ParentDomainFormModal
        open={mode === 'parent'}
        domains={flatDomains}
        confirmLoading={saving}
        onCancel={() => setMode(undefined)}
        onSubmit={saveParent}
      />
    </Card>
  );
}

function toTreeNode(node: DomainNode): any {
  return {
    key: node.id,
    title: node.name,
    source: node,
    children: (node.children ?? []).map(toTreeNode),
  };
}
