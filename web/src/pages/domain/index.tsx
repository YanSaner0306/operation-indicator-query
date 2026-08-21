import { Card, Col, Descriptions, Row, Tag, Typography } from 'antd';
import DomainTreePanel from '@/components/domain/DomainTreePanel';
import { useDomainTree } from '@/hooks/useDomainTree';
import { statusText } from '@/utils/ontology';

export default function DomainPage() {
  const domains = useDomainTree();

  return (
    <div>
      <Typography.Title level={3}>业务领域</Typography.Title>
      <Typography.Paragraph type="secondary">
        维护本体分类树。领域存在子节点或已关联本体时不能删除。
      </Typography.Paragraph>
      <Row gutter={16} align="stretch">
        <Col xs={24} lg={9} xl={7}>
          <DomainTreePanel
            tree={domains.tree}
            flatDomains={domains.flatDomains}
            loading={domains.loading}
            selectedId={domains.selectedId}
            onSelect={domains.setSelectedId}
            onChanged={domains.refresh}
          />
        </Col>
        <Col xs={24} lg={15} xl={17}>
          <Card title="领域详情" style={{ minHeight: 360 }}>
            {domains.selectedDomain ? (
              <Descriptions bordered column={1}>
                <Descriptions.Item label="名称">{domains.selectedDomain.name}</Descriptions.Item>
                <Descriptions.Item label="编码">{domains.selectedDomain.code}</Descriptions.Item>
                <Descriptions.Item label="状态">
                  <Tag color={domains.selectedDomain.status === 'ENABLED' ? 'green' : 'default'}>
                    {statusText[domains.selectedDomain.status]}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="同级排序">{domains.selectedDomain.sortOrder}</Descriptions.Item>
                <Descriptions.Item label="说明">
                  {domains.selectedDomain.description || '-'}
                </Descriptions.Item>
              </Descriptions>
            ) : (
              <Typography.Text type="secondary">请从左侧选择领域节点。</Typography.Text>
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
}
