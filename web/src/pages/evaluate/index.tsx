import { useEffect, useState } from 'react';
import { Card, Space, Spin, Tag, Typography } from 'antd';
import { evaluateApi } from '@/api/evaluate';

export default function EvaluatePage() {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    evaluateApi.ping()
      .then((d) => setData(d.data ?? d))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Card title="evaluate 模块" extra={<Tag color="blue">占位</Tag>}>
      <Space direction="vertical">
        <Typography.Text type="secondary">
          evaluate 模块占位页，后续按需求落地具体功能。
        </Typography.Text>
        {loading ? (
          <Spin />
        ) : (
          <pre style={{ background: '#fafafa', padding: 12, borderRadius: 6 }}>
            {JSON.stringify(data, null, 2)}
          </pre>
        )}
      </Space>
    </Card>
  );
}
