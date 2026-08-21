import { useEffect, useState } from 'react';
import { Button, Descriptions, Drawer, Empty, List, message, Space, Tag, Typography } from 'antd';
import { ruleApi } from '@/api/rule';
import { getErrorMessage } from '@/api/client';
import type { Rule, RuleVersion } from '@/types/rule';
import { formatDateTime, operatorText } from '@/utils/ontology';

interface Props {
  open: boolean;
  rule?: Rule;
  onClose: () => void;
  onChanged: () => void;
}

export default function RuleVersionDrawer({ open, rule, onClose, onChanged }: Props) {
  const [items, setItems] = useState<RuleVersion[]>([]);
  const [loading, setLoading] = useState(false);
  const [switching, setSwitching] = useState<number>();

  useEffect(() => {
    if (!open || !rule) return;
    setLoading(true);
    ruleApi.versions(rule.id)
      .then(setItems)
      .catch((error) => message.error(getErrorMessage(error)))
      .finally(() => setLoading(false));
  }, [open, rule]);

  const switchVersion = async (version: RuleVersion) => {
    if (!rule || version.id === rule.currentVersionId) return;
    setSwitching(version.id);
    try {
      await ruleApi.switchVersion(rule.id, version.id, rule.version);
      message.success(`已切换到 v${version.versionNo}`);
      onChanged();
      onClose();
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setSwitching(undefined);
    }
  };

  return (
    <Drawer open={open} width={640} title={rule ? `${rule.name} · 版本记录` : '版本记录'} onClose={onClose}>
      {items.length ? (
        <List
          loading={loading}
          dataSource={items}
          renderItem={(item) => (
            <List.Item
              actions={[
                <Button
                  key="switch"
                  type={item.id === rule?.currentVersionId ? 'default' : 'link'}
                  disabled={item.id === rule?.currentVersionId}
                  loading={switching === item.id}
                  onClick={() => void switchVersion(item)}
                >
                  {item.id === rule?.currentVersionId ? '当前版本' : '切换'}
                </Button>,
              ]}
            >
              <List.Item.Meta
                title={<Space><Tag color={item.id === rule?.currentVersionId ? 'blue' : 'default'}>v{item.versionNo}</Tag><Typography.Text>{item.changeNote || '无变更说明'}</Typography.Text></Space>}
                description={(
                  <Descriptions size="small" column={1}>
                    <Descriptions.Item label="条件">
                      {item.condition.propertyName} {operatorText[item.condition.operator]} {item.condition.compareValue || ''}
                    </Descriptions.Item>
                    <Descriptions.Item label="结果">{item.action.resultName}（{item.action.resultCode}）</Descriptions.Item>
                    <Descriptions.Item label="创建时间">{formatDateTime(item.createdAt)}</Descriptions.Item>
                  </Descriptions>
                )}
              />
            </List.Item>
          )}
        />
      ) : !loading ? <Empty description="暂无版本" /> : null}
    </Drawer>
  );
}
