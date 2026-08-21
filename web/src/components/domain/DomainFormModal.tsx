import { useEffect } from 'react';
import { Form, Input, InputNumber, Modal, Select } from 'antd';
import type { DomainNode, DomainPayload } from '@/types/ontology';
import type { FlatDomain } from '@/utils/ontology';

interface Props {
  open: boolean;
  domain?: DomainNode;
  parentId?: number;
  domains: FlatDomain[];
  allowParentSelection?: boolean;
  confirmLoading?: boolean;
  onCancel: () => void;
  onSubmit: (values: DomainPayload) => Promise<void>;
}

type DomainFormValues = Omit<DomainPayload, 'parentId'> & {
  parentId?: number | null | typeof ROOT_PARENT_VALUE;
};

export default function DomainFormModal({
  open,
  domain,
  parentId,
  domains,
  allowParentSelection = true,
  confirmLoading,
  onCancel,
  onSubmit,
}: Props) {
  const [form] = Form.useForm<DomainFormValues>();

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue({
      parentId: domain?.parentId ?? parentId ?? ROOT_PARENT_VALUE,
      name: domain?.name ?? '',
      code: domain?.code ?? '',
      description: domain?.description ?? '',
      status: domain?.status ?? 'ENABLED',
      sortOrder: domain?.sortOrder ?? 0,
      version: domain?.version,
    });
  }, [domain, form, open, parentId]);

  return (
    <Modal
      open={open}
      title={domain ? '编辑领域' : '新增领域'}
      okText="保存"
      cancelText="取消"
      confirmLoading={confirmLoading}
      onCancel={onCancel}
      onOk={() => form.validateFields().then((values) => {
        const parentId = values.parentId === ROOT_PARENT_VALUE ? null : values.parentId ?? null;
        return onSubmit({ ...values, parentId });
      })}
      forceRender
    >
      <Form form={form} layout="vertical" preserve={false}>
        {allowParentSelection && (
          <Form.Item
            name="parentId"
            label="父领域"
            extra={!domain ? '选择“无”时创建为独立领域' : undefined}
          >
            <Select
              allowClear
              placeholder="无（独立领域）"
              options={[
                { value: ROOT_PARENT_VALUE, label: '无（独立领域）' },
                ...domains.map((item) => ({
                  value: item.id,
                  label: `${'　'.repeat(item.level)}${item.name}`,
                  disabled: item.id === domain?.id,
                })),
              ]}
            />
          </Form.Item>
        )}
        <Form.Item name="name" label="领域名称" rules={[{ required: true }, { max: 64 }]}>
          <Input placeholder="例如：采购管理" />
        </Form.Item>
        <Form.Item
          name="code"
          label="领域编码"
          rules={[
            { required: true },
            { max: 64 },
            { pattern: /^[A-Za-z][A-Za-z0-9_]*$/, message: '仅支持字母、数字和下划线' },
          ]}
        >
          <Input placeholder="例如：PROCUREMENT" />
        </Form.Item>
        <Form.Item name="description" label="说明" rules={[{ max: 500 }]}>
          <Input.TextArea rows={3} />
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true }]}>
          <Select options={[{ value: 'ENABLED', label: '启用' }, { value: 'DISABLED', label: '禁用' }]} />
        </Form.Item>
        <Form.Item name="sortOrder" label="同级排序" rules={[{ required: true }]}>
          <InputNumber min={0} precision={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="version" hidden><Input /></Form.Item>
      </Form>
    </Modal>
  );
}

const ROOT_PARENT_VALUE = '__ROOT_PARENT__';
