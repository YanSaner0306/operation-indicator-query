import { useEffect } from 'react';
import { Form, Input, InputNumber, Modal, Select } from 'antd';
import type { ParentDomainPayload } from '@/types/ontology';
import type { FlatDomain } from '@/utils/ontology';

interface Props {
  open: boolean;
  domains: FlatDomain[];
  confirmLoading?: boolean;
  onCancel: () => void;
  onSubmit: (values: ParentDomainPayload) => Promise<void>;
}

export default function ParentDomainFormModal({
  open,
  domains,
  confirmLoading,
  onCancel,
  onSubmit,
}: Props) {
  const [form] = Form.useForm<ParentDomainPayload>();
  const independentDomains = domains.filter(
    (item) => item.parentId == null && (item.children?.length ?? 0) === 0,
  );

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue({
      name: '',
      code: '',
      description: '',
      status: 'ENABLED',
      sortOrder: 0,
      childDomainIds: [],
    });
  }, [form, open]);

  return (
    <Modal
      open={open}
      title="新增父领域"
      okText="创建并归组"
      cancelText="取消"
      confirmLoading={confirmLoading}
      onCancel={onCancel}
      onOk={() => form.validateFields().then(onSubmit)}
      forceRender
    >
      <Form form={form} layout="vertical" preserve={false}>
        <Form.Item
          name="childDomainIds"
          label="归组领域"
          rules={[{ required: true, message: '请至少选择一个已有独立领域' }]}
          extra="所选领域会统一移动到新建父领域下面"
        >
          <Select
            mode="multiple"
            showSearch
            optionFilterProp="label"
            placeholder="选择需要归组的独立领域"
            options={independentDomains.map((item) => ({
              value: item.id,
              label: `${item.name}（${item.code}）`,
            }))}
          />
        </Form.Item>
        <Form.Item name="name" label="父领域名称" rules={[{ required: true }, { max: 64 }]}>
          <Input placeholder="例如：供应链管理" />
        </Form.Item>
        <Form.Item
          name="code"
          label="父领域编码"
          rules={[
            { required: true },
            { max: 64 },
            { pattern: /^[A-Za-z][A-Za-z0-9_]*$/, message: '仅支持字母、数字和下划线' },
          ]}
        >
          <Input placeholder="例如：SUPPLY_CHAIN" />
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
      </Form>
    </Modal>
  );
}
