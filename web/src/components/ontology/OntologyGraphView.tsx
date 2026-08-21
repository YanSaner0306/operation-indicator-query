import { useEffect, useMemo, useState } from 'react';
import { Empty, message, Spin, Tag, Typography } from 'antd';
import { ontologyApi } from '@/api/ontology';
import { getErrorMessage } from '@/api/client';
import type { OntologyGraph } from '@/types/ontology';

interface Props {
  domainId?: number;
  focusOntologyId?: number;
}

export default function OntologyGraphView({ domainId, focusOntologyId }: Props) {
  const [graph, setGraph] = useState<OntologyGraph>({ nodes: [], edges: [] });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    ontologyApi.graph(domainId)
      .then(setGraph)
      .catch((error) => message.error(getErrorMessage(error)))
      .finally(() => setLoading(false));
  }, [domainId]);

  const visibleGraph = useMemo(() => {
    if (focusOntologyId == null) return graph;

    const edges = graph.edges.filter(
      (edge) => edge.sourceOntologyId === focusOntologyId || edge.targetOntologyId === focusOntologyId,
    );
    const nodeIds = new Set<number>([focusOntologyId]);
    edges.forEach((edge) => {
      nodeIds.add(edge.sourceOntologyId);
      nodeIds.add(edge.targetOntologyId);
    });
    return {
      nodes: graph.nodes.filter((node) => nodeIds.has(node.id)),
      edges,
    };
  }, [focusOntologyId, graph]);

  const nodeMap = useMemo(
    () => new Map(visibleGraph.nodes.map((node) => [node.id, node])),
    [visibleGraph.nodes],
  );

  return (
    <Spin spinning={loading}>
      {visibleGraph.nodes.length ? (
        <div className="ontology-graph">
          <div className="graph-nodes">
            {visibleGraph.nodes.map((node) => (
              <div className="graph-node" key={node.id}>
                <Typography.Text strong>{node.name}</Typography.Text>
                <Typography.Text type="secondary">{node.code}</Typography.Text>
              </div>
            ))}
          </div>
          <div className="graph-edges">
            {visibleGraph.edges.map((edge) => (
              <div className="graph-edge" key={edge.id}>
                <span>{nodeMap.get(edge.sourceOntologyId)?.name ?? edge.sourceOntologyId}</span>
                <span className="graph-arrow">→</span>
                <Tag color="blue">{edge.name} · {edge.cardinality}</Tag>
                <span className="graph-arrow">→</span>
                <span>{nodeMap.get(edge.targetOntologyId)?.name ?? edge.targetOntologyId}</span>
              </div>
            ))}
          </div>
        </div>
      ) : (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当前范围暂无启用关系" />
      )}
    </Spin>
  );
}
