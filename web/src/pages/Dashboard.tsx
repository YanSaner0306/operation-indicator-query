import { Card, Col, Row, Tag, Typography } from 'antd';

const modules = [
  { name: '本体管理', desc: '左侧领域树、本体、属性、关系和只读图谱', ready: true },
  { name: '规则管理', desc: '单属性规则、版本和手工测试', ready: true },
  { name: '策略管理', desc: 'SOP、反馈与优化', ready: false },
  { name: '风险管理', desc: '风险编目与应对', ready: false },
  { name: '数据管理', desc: '数据源、Binding 与即时查询', ready: false },
  { name: '对接管理', desc: 'API、Token 和外部调用', ready: false },
  { name: '测试评估', desc: '质量验证与效果评估', ready: false },
];

export default function Dashboard() {
  return (
    <div>
      <Typography.Title level={3}>系统总览</Typography.Title>
      <Typography.Paragraph type="secondary">
        一期本体与规则模块已经接入现有 Spring Boot API，其余模块继续按统一框架扩展。
      </Typography.Paragraph>
      <Row gutter={[16, 16]}>
        {modules.map((module) => (
          <Col key={module.name} xs={24} sm={12} lg={8} xl={6}>
            <Card
              title={module.name}
              extra={<Tag color={module.ready ? 'green' : 'default'}>{module.ready ? '已接入' : '待建设'}</Tag>}
              className="module-card"
            >
              <Typography.Text type="secondary">{module.desc}</Typography.Text>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
