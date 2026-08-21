import { useEffect, useMemo, useState } from 'react';
import {
  Button, Card, Form, Input, InputNumber, message, Select, Space, Steps, Switch,
} from 'antd';
import { ontologyApi } from '@/api/ontology';
import { getErrorMessage } from '@/api/client';
import type { Ontology, OntologyProperty } from '@/types/ontology';
import type { Rule, RuleOperator, RulePayload } from '@/types/rule';
import { operatorText, operatorsForType, propertyTypeText } from '@/utils/ontology';

interface Props {
  rule?: Rule;
  initialOntologyId?: number;
  ontologies: Ontology[];
  saving?: boolean;
  onCancel: () => void;
  onSubmit: (payload: RulePayload) => Promise<void>;
}

export default function RuleEditorForm({
  rule,
  initialOntologyId,
  ontologies,
  saving,
  onCancel,
  onSubmit,
}: Props) {
  const [form] = Form.useForm<any>();
  const [step, setStep] = useState(0);
  const [properties, setProperties] = useState<OntologyProperty[]>([]);
  const ontologyId = Form.useWatch('ontologyId', form);
  const propertyId = Form.useWatch(['condition', 'propertyId'], form);
  const operator = Form.useWatch(['condition', 'operator'], form) as RuleOperator | undefined;
  const selectedProperty = properties.find((item) => item.id === propertyId);
  const operators = useMemo(() => operatorsForType(selectedProperty?.dataType), [selectedProperty?.dataType]);

  useEffect(() => {
    setStep(0);
    form.setFieldsValue({
      name: rule?.name ?? '',
      code: rule?.code ?? '',
      ontologyId: rule?.ontologyId ?? initialOntologyId,
      description: rule?.description ?? '',
      enabled: rule?.enabled ?? true,
      condition: {
        propertyId: rule?.condition.propertyId,
        operator: rule?.condition.operator,
        compareValue: rule?.condition.compareValue,
      },
      action: {
        resultCode: rule?.action.resultCode ?? '',
        resultName: rule?.action.resultName ?? '',
        message: rule?.action.message ?? '',
      },
      changeNote: rule ? '' : '初始版本',
      version: rule?.version,
    });
  }, [form, initialOntologyId, rule]);

  useEffect(() => {
    if (!ontologyId) {
      setProperties([]);
      return;
    }
    ontologyApi.properties(ontologyId)
      .then((items) => setProperties(items.filter((item) => item.status === 'ENABLED')))
      .catch((error) => message.error(getErrorMessage(error)));
  }, [ontologyId]);

  useEffect(() => {
    if (operator && operators.length && !operators.includes(operator)) {
      form.setFieldValue(['condition', 'operator'], undefined);
    }
  }, [form, operator, operators]);

  const next = async () => {
    const fields = step === 0
      ? ['name', 'code', 'ontologyId']
      : [['condition', 'propertyId'], ['condition', 'operator'], ['condition', 'compareValue']];
    await form.validateFields(fields as any);
    setStep((value) => value + 1);
  };

  const submit = async () => {
    const values = await form.validateFields();
    await onSubmit({
      name: values.name,
      code: values.code,
      ontologyId: rule ? undefined : values.ontologyId,
      description: values.description,
      enabled: rule ? undefined : values.enabled,
      condition: values.condition,
      action: values.action,
      changeNote: values.changeNote,
      version: rule?.version,
    });
  };

  const emptyOperator = operator === 'IS_EMPTY' || operator === 'IS_NOT_EMPTY';

  return (
    <Card className="rule-editor-card">
      <Steps
        current={step}
        className="rule-editor-steps"
        items={[{ title: '基本信息' }, { title: '条件配置' }, { title: '执行动作' }]}
      />
      <Form
        form={form}
        layout="horizontal"
        labelCol={{ span: 5 }}
        wrapperCol={{ span: 16 }}
        preserve
        className="rule-step-form"
      >
        <div hidden={step !== 0}>
          <Form.Item name="name" label="规则名称" rules={[{ required: true }, { max: 100 }]}><Input /></Form.Item>
          <Form.Item name="code" label="规则编码" rules={[{ required: true }, { pattern: /^[A-Za-z][A-Za-z0-9_]*$/ }]}><Input /></Form.Item>
          <Form.Item name="ontologyId" label="所属本体" rules={[{ required: true }]}> 
            <Select
              disabled={Boolean(rule) || Boolean(initialOntologyId)}
              showSearch
              optionFilterProp="label"
              options={ontologies.map((item) => ({
                value: item.id,
                label: item.name,
                disabled: item.status === 'DISABLED',
              }))}
            />
          </Form.Item>
          <Form.Item name="description" label="规则描述" rules={[{ max: 500 }]}><Input.TextArea rows={4} /></Form.Item>
          {!rule && <Form.Item name="enabled" label="状态" valuePropName="checked"><Switch checkedChildren="启用" unCheckedChildren="禁用" /></Form.Item>}
        </div>

        <div hidden={step !== 1}>
          <Form.Item name={['condition', 'propertyId']} label="规则属性" rules={[{ required: true }]}> 
            <Select
              showSearch
              optionFilterProp="label"
              options={properties.map((item) => ({ value: item.id, label: `${item.name} · ${propertyTypeText[item.dataType]}` }))}
            />
          </Form.Item>
          <Form.Item name={['condition', 'operator']} label="操作符" rules={[{ required: true }]}> 
            <Select disabled={!selectedProperty} options={operators.map((value) => ({ value, label: operatorText[value] }))} />
          </Form.Item>
          {!emptyOperator && (
            <Form.Item name={['condition', 'compareValue']} label="比较值" rules={[{ required: true }]}> 
              {selectedProperty?.dataType === 'INTEGER' || selectedProperty?.dataType === 'DECIMAL' ? (
                <InputNumber style={{ width: '100%' }} />
              ) : selectedProperty?.dataType === 'BOOLEAN' ? (
                <Select options={[{ value: true, label: '是' }, { value: false, label: '否' }]} />
              ) : (
                <Input placeholder={selectedProperty?.dataType === 'DATE' ? 'YYYY-MM-DD' : selectedProperty?.dataType === 'DATETIME' ? 'YYYY-MM-DDTHH:mm:ss' : '请输入比较值'} />
              )}
            </Form.Item>
          )}
        </div>

        <div hidden={step !== 2}>
          <Form.Item name={['action', 'resultCode']} label="结果编码" rules={[{ required: true }, { pattern: /^[A-Za-z][A-Za-z0-9_]*$/ }]}><Input /></Form.Item>
          <Form.Item name={['action', 'resultName']} label="结果名称" rules={[{ required: true }, { max: 100 }]}><Input /></Form.Item>
          <Form.Item name={['action', 'message']} label="提示信息" rules={[{ max: 1000 }]}><Input.TextArea rows={4} /></Form.Item>
          <Form.Item name="changeNote" label="版本说明" rules={[{ max: 500 }]}><Input.TextArea rows={3} /></Form.Item>
        </div>
      </Form>

      <div className="rule-editor-actions">
        <Button onClick={onCancel}>取消</Button>
        <Space>
          {step > 0 && <Button onClick={() => setStep((value) => value - 1)}>上一步</Button>}
          {step < 2 ? (
            <Button type="primary" onClick={() => void next()}>
              {step === 0 ? '下一步：条件配置' : '下一步：执行动作'}
            </Button>
          ) : (
            <Button type="primary" loading={saving} onClick={() => void submit()}>保存规则</Button>
          )}
        </Space>
      </div>
    </Card>
  );
}
