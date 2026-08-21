package com.biz.ontology.domain.service;

import com.biz.ontology.domain.model.DomainEntity;
import com.biz.ontology.domain.repository.DomainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DomainHierarchyService {

    private final DomainRepository domainRepository;

    public DomainHierarchyService(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @Transactional(readOnly = true)
    public Set<Long> getSelfAndDescendantIds(Long domainId) {
        List<DomainEntity> domains = domainRepository.findAllByOrderBySortOrderAscIdAsc();
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (DomainEntity domain : domains) {
            if (domain.getParentId() != null) {
                childrenByParent.computeIfAbsent(domain.getParentId(), ignored -> new ArrayList<>())
                        .add(domain.getId());
            }
        }

        Set<Long> result = new LinkedHashSet<>();
        ArrayDeque<Long> pending = new ArrayDeque<>();
        pending.add(domainId);
        while (!pending.isEmpty()) {
            Long currentId = pending.removeFirst();
            if (!result.add(currentId)) {
                continue;
            }
            pending.addAll(childrenByParent.getOrDefault(currentId, List.of()));
        }
        return result;
    }
}
