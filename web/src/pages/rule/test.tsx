import { useCallback, useEffect, useState } from 'react';
import {
  Button, Card, Col, Empty, Form, Input, InputNumber, message, Row, Select,
  Space, Spin, Tag, Typography,
} from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { ontologyApi } from '@/api/ontology';
import { ruleApi } from '@/api/rule';
import { getErrorMessage } from '@/api/client';
import type { OntologyProperty } from '@/types/ontology';
import type { Rule, RuleTestResult } from '@/types/rule';
import { formatDateTime, operatorText, propertyTypeText } from '@/utils/ontology';

export default function RuleTestPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const ruleId = Number(id);
  const [form] = Form.useForm<{ value: unknown }>();
  const [rule, setRule] = useState<Rule>();
  const [property, setProperty] = useState<OntologyProperty>();
  const [loading, setLoading] = useState(true);
  const [testing, setTesting] = useState(false);
  const [result, setResult] = useState<RuleTestResult>();
  const [testedAt, setTestedAt] = useState<string>();

  const load = useCallback(async () => {
    if (!Number.isFinite(ruleId)) return;
    setLoading(true);
    try {
      const detail = await ruleApi.get(ruleId);
      const properties = await ontologyApi.properties(detail.ontologyId);
      setRule(detail);
      setProperty(properties.find((item) => item.id === detail.condition.propertyId));
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [ruleId]);

  useEffect(() => { void load(); }, [load]);

  const run = async () => {
    if (!rule || !property) return;
    const values = await form.validateFields();
    setTesting(true);
    try {
      setResult(await ruleApi.test(rule.id, rule.currentVersionId, { [property.id]: values.value }));
      setTestedAt(new Date().toISOString());
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setTesting(false);
    }
  };

  if (loading) return <Spin />;
  if (!rule || !property) return <Card><Empty description="规则或规则属性不存在" /></Card>;

  const editor = property.dataType === 'INTEGER' || property.dataType === 'DECIMAL'
    ? <InputNumber style={{ width: '100%' }} />
    : property.dataType === 'BOOLEAN'
      ? <Select options={[{ value: true, label: '是' }, { value: false, label: '否' }]} />
      : <Input placeholder={`请输入${propertyTypeText[property.dataType]}值`} />;

  return (
    <div className="rule-test-page">
      <div className="detail-page-header">
        <Button type="text" onClick={() => navigate('/rules')}>‹ 返回规则列表</Button>
        <Typography.Title level={3}>规则测试</Typography.Title>
      </div>

      <Card className="rule-test-card">
        <Typography.Paragraph className="rule-test-summary">
          规则：<Typography.Text strong>{rule.name}</Typography.Text>
          （{property.name} {operatorText[rule.condition.operator]} {rule.condition.compareValue ?? ''}）
        </Typography.Paragraph>

        <Row gutter={16} align="stretch">
          <Col xs={24} md={11}>
            <Card size="small" title="测试数据（手动输入本体属性值）">
              <Form form={form} layout="vertical">
                <Form.Item name="value" label={property.name} rules={[{ required: true, message: '请输入测试值' }]}>
                  {editor}
                </Form.Item>
                <div className="rule-test-run-action">
                  <Button type="primary" loading={testing} onClick={() => void run()}>开始测试</Button>
                </div>
              </Form>
            </Card>
            <Card size="small" title="涉及属性说明" style={{ marginTop: 12 }}>
              <Space direction="vertical" size={8}>
                <Typography.Text>● 本体：{rule.ontologyName}</Typography.Text>
                <Typography.Text>● 属性：{property.name}（{propertyTypeText[property.dataType]}）</Typography.Text>
              </Space>
            </Card>
          </Col>
          <Col xs={24} md={13}>
            <Card size="small" title="测试结果" className="rule-test-result-card">
              {result ? (
                <div className={result.matched ? 'rule-result-panel matched' : 'rule-result-panel'}>
                  <Tag color={result.matched ? 'green' : 'default'}>{result.matched ? '命中' : '未命中'}</Tag>
                  <div className="rule-result-lines">
                    <Typography.Text>实际值：{String(result.condition.actualValue ?? '')}</Typography.Text>
                    <Typography.Text>条件：{result.condition.propertyName} {operatorText[result.condition.operator]} {result.condition.expectedValue ?? ''}</Typography.Text>
                    <Typography.Text>
                      结果：{result.action
                        ? `${result.action.resultName}${result.action.message ? `（${result.action.message}）` : ''}`
                        : result.matched ? '命中规则条件但无执行动作' : '未命中，不执行动作'}
                    </Typography.Text>
                    <Typography.Text>版本：v{result.versionNo}</Typography.Text>
                    <Typography.Text type="secondary">测试时间：{testedAt ? formatDateTime(testedAt) : '-'}</Typography.Text>
                  </div>
                </div>
              ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="输入测试值后查看结果" />
              )}
            </Card>
          </Col>
        </Row>
      </Card>
    </div>
  );
}
