import { useCallback, useEffect, useState } from 'react';
import { Button, message, Spin, Typography } from 'antd';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { ontologyApi } from '@/api/ontology';
import { ruleApi } from '@/api/rule';
import { getErrorMessage } from '@/api/client';
import RuleEditorForm from '@/components/rule/RuleEditorForm';
import type { Ontology } from '@/types/ontology';
import type { Rule, RulePayload } from '@/types/rule';

export default function RuleEditorPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const ruleId = id ? Number(id) : undefined;
  const initialOntologyId = searchParams.get('ontologyId') ? Number(searchParams.get('ontologyId')) : undefined;
  const [rule, setRule] = useState<Rule>();
  const [ontologies, setOntologies] = useState<Ontology[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [ontologyPage, detail] = await Promise.all([
        ontologyApi.page({ page: 0, size: 100 }),
        ruleId ? ruleApi.get(ruleId) : Promise.resolve(undefined),
      ]);
      setOntologies(ontologyPage.items);
      setRule(detail);
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [ruleId]);

  useEffect(() => { void load(); }, [load]);

  const save = async (payload: RulePayload) => {
    setSaving(true);
    try {
      const saved = rule ? await ruleApi.update(rule.id, payload) : await ruleApi.create(payload);
      message.success(rule ? '规则新版本保存成功' : '规则创建成功');
      navigate(`/rules?ontologyId=${saved.ontologyId}`);
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Spin />;

  return (
    <div className="rule-editor-page">
      <div className="detail-page-header">
        <Button type="text" onClick={() => navigate('/rules')}>‹ 返回规则列表</Button>
        <div className="detail-page-title">
          <Typography.Title level={3}>{rule ? '编辑规则' : '新增规则'}</Typography.Title>
          {rule && <Typography.Text type="secondary">当前 v{rule.currentVersionNo}，保存后生成新版本</Typography.Text>}
        </div>
      </div>
      <RuleEditorForm
        rule={rule}
        initialOntologyId={initialOntologyId}
        ontologies={ontologies}
        saving={saving}
        onCancel={() => navigate('/rules')}
        onSubmit={save}
      />
    </div>
  );
}
