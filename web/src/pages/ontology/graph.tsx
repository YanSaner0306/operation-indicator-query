import { useCallback, useEffect, useState } from 'react';
import { Button, Card, message, Spin, Tag, Typography } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { ontologyApi } from '@/api/ontology';
import { getErrorMessage } from '@/api/client';
import OntologyGraphView from '@/components/ontology/OntologyGraphView';
import type { Ontology } from '@/types/ontology';
import { statusText } from '@/utils/ontology';

export default function OntologyGraphPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const ontologyId = Number(id);
  const [ontology, setOntology] = useState<Ontology>();
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    if (!Number.isFinite(ontologyId)) return;
    setLoading(true);
    try {
      setOntology(await ontologyApi.get(ontologyId));
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [ontologyId]);

  useEffect(() => { void load(); }, [load]);

  if (loading) return <Card><Spin /></Card>;
  if (!ontology) return <Card><Typography.Text type="secondary">本体不存在或已被删除。</Typography.Text></Card>;

  return (
    <div className="ontology-detail-page">
      <div className="detail-page-header">
        <Button type="text" onClick={() => navigate(`/ontology/${ontology.id}`)}>‹ 返回本体详情</Button>
        <div className="detail-page-title">
          <Typography.Title level={4}>{ontology.name} · 关系图谱</Typography.Title>
          <Typography.Text type="secondary">{ontology.code}</Typography.Text>
          <Tag color={ontology.status === 'ENABLED' ? 'green' : 'default'}>{statusText[ontology.status]}</Tag>
        </div>
      </div>
      <Card title="只读关系图谱">
        <OntologyGraphView focusOntologyId={ontology.id} />
      </Card>
    </div>
  );
}
