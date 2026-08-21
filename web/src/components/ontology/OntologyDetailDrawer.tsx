import { Descriptions, Drawer, Tabs, Tag, Typography } from 'antd';
import type { Ontology } from '@/types/ontology';
import { formatDateTime, statusText } from '@/utils/ontology';
import PropertyPanel from './PropertyPanel';
import RelationPanel from './RelationPanel';
import OntologyGraphView from './OntologyGraphView';
import OntologyRulePanel from './OntologyRulePanel';

interface Props {
  open: boolean;
  ontology?: Ontology;
  ontologies: Ontology[];
  onClose: () => void;
  onChanged: () => void;
}

export default function OntologyDetailDrawer({ open, ontology, ontologies, onClose, onChanged }: Props) {
  return (
    <Drawer
      open={open}
      width="min(1080px, 94vw)"
      title={ontology ? `${ontology.name} · ${ontology.code}` : '本体详情'}
      onClose={onClose}
      destroyOnHidden
    >
      {ontology && (
        <Tabs
          items={[
            {
              key: 'base',
              label: '基本信息',
              children: (
                <Descriptions bordered column={2}>
                  <Descriptions.Item label="名称">{ontology.name}</Descriptions.Item>
                  <Descriptions.Item label="编码">{ontology.code}</Descriptions.Item>
                  <Descriptions.Item label="状态">
                    <Tag color={ontology.status === 'ENABLED' ? 'green' : 'default'}>{statusText[ontology.status]}</Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="版本">{ontology.version}</Descriptions.Item>
                  <Descriptions.Item label="属性数">{ontology.propertyCount}</Descriptions.Item>
                  <Descriptions.Item label="关系数">{ontology.relationCount}</Descriptions.Item>
                  <Descriptions.Item label="规则数">{ontology.ruleCount}</Descriptions.Item>
                  <Descriptions.Item label="更新时间">{formatDateTime(ontology.updatedAt)}</Descriptions.Item>
                  <Descriptions.Item label="说明" span={2}>
                    <Typography.Text>{ontology.description || '-'}</Typography.Text>
                  </Descriptions.Item>
                </Descriptions>
              ),
            },
            {
              key: 'properties',
              label: `属性 (${ontology.propertyCount})`,
              children: <PropertyPanel ontologyId={ontology.id} onChanged={onChanged} />,
            },
            {
              key: 'relations',
              label: `关系 (${ontology.relationCount})`,
              children: <RelationPanel ontologyId={ontology.id} ontologies={ontologies} onChanged={onChanged} />,
            },
            {
              key: 'rules',
              label: `规则 (${ontology.ruleCount})`,
              children: <OntologyRulePanel ontologyId={ontology.id} />,
            },
            {
              key: 'graph',
              label: '关系图谱',
              children: <OntologyGraphView domainId={ontology.domainIds[0]} />,
            },
          ]}
        />
      )}
    </Drawer>
  );
}
