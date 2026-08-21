import { useCallback, useEffect, useMemo, useState } from 'react';
import { message } from 'antd';
import { domainApi } from '@/api/domain';
import { getErrorMessage } from '@/api/client';
import type { DomainNode } from '@/types/ontology';
import { flattenDomains } from '@/utils/ontology';

export function useDomainTree() {
  const [tree, setTree] = useState<DomainNode[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedId, setSelectedId] = useState<number>();

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const data = await domainApi.tree();
      setTree(data);
      if (selectedId && !flattenDomains(data).some((item) => item.id === selectedId)) {
        setSelectedId(undefined);
      }
    } catch (error) {
      message.error(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [selectedId]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const flatDomains = useMemo(() => flattenDomains(tree), [tree]);
  const selectedDomain = flatDomains.find((item) => item.id === selectedId);

  return {
    tree,
    flatDomains,
    loading,
    selectedId,
    selectedDomain,
    setSelectedId,
    refresh,
  };
}
