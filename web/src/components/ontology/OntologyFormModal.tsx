import { useEffect } from 'react';
import { Form, Input, Modal, Select } from 'antd';
import type { Ontology, OntologyPayload } from '@/types/ontology';
import type { FlatDomain } from '@/utils/ontology';

interface Props {
  open: boolean;
  ontology?: Ontology;
  domains: FlatDomain[];
  initialDomainId?: number;
  confirmLoading?: boolean;
  onCancel: () => void;
  onSubmit: (payload: OntologyPayload) => Promise<void>;
}

export default function OntologyFormModal({
  open,
  ontology,
  domains,
  initialDomainId,
  confirmLoading,
  onCancel,
  onSubmit,
}: Props) {
  const [form] = Form.useForm<OntologyPayload>();

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue({
      name: ontology?.name ?? '',
      code: ontology?.code ?? '',
      description: ontology?.description ?? '',
      status: ontology?.status ?? 'ENABLED',
      domainIds: ontology?.domainIds ?? (initialDomainId ? [initialDomainId] : []),
      version: ontology?.version,
    });
  }, [form, initialDomainId, ontology, open]);

  return (
    <Modal
      open={open}
      title={ontology ? '编辑本体' : '新增本体'}
      width={620}
      okText="保存"
      cancelText="取消"
      confirmLoading={confirmLoading}
      onCancel={onCancel}
      onOk={() => form.validateFields().then(onSubmit)}
      forceRender
    >
      <Form form={form} layout="vertical" preserve={false}>
        <Form.Item name="name" label="本体名称" rules={[{ required: true }, { max: 64 }]}>
          <Input placeholder="例如：采购订单" />
        </Form.Item>
        <Form.Item
          name="code"
          label="本体编码"
          rules={[
            { required: true },
            { max: 64 },
            { pattern: /^[A-Za-z][A-Za-z0-9_]*$/, message: '仅支持字母、数字和下划线' },
          ]}
        >
          <Input placeholder="例如：PURCHASE_ORDER" />
        </Form.Item>
        <Form.Item name="domainIds" label="所属领域">
          <Select
            mode="multiple"
            allowClear
            placeholder="本体可以暂时不归类"
            options={domains.map((item) => ({
              value: item.id,
              label: `${'　'.repeat(item.level)}${item.name}`,
              disabled: item.status === 'DISABLED' && !ontology?.domainIds.includes(item.id),
            }))}
          />
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true }]}>
          <Select options={[{ value: 'ENABLED', label: '启用' }, { value: 'DISABLED', label: '禁用' }]} />
        </Form.Item>
        <Form.Item name="description" label="说明" rules={[{ max: 500 }]}>
          <Input.TextArea rows={4} />
        </Form.Item>
        <Form.Item name="version" hidden><Input /></Form.Item>
      </Form>
    </Modal>
  );
}
