package com.biz.ontology.ontology.service;

import com.biz.ontology.api.ontology.dto.OntologyGraphResponse;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.domain.service.DomainHierarchyService;
import com.biz.ontology.ontology.model.DomainOntologyRelEntity;
import com.biz.ontology.ontology.model.OntologyEntity;
import com.biz.ontology.ontology.model.OntologyRelationEntity;
import com.biz.ontology.ontology.repository.DomainOntologyRelRepository;
import com.biz.ontology.ontology.repository.OntologyRelationRepository;
import com.biz.ontology.ontology.repository.OntologyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class OntologyGraphService {

    private final OntologyRepository ontologyRepository;
    private final OntologyRelationRepository relationRepository;
    private final DomainOntologyRelRepository domainRelRepository;
    private final DomainHierarchyService domainHierarchyService;

    public OntologyGraphService(
            OntologyRepository ontologyRepository,
            OntologyRelationRepository relationRepository,
            DomainOntologyRelRepository domainRelRepository,
            DomainHierarchyService domainHierarchyService) {
        this.ontologyRepository = ontologyRepository;
        this.relationRepository = relationRepository;
        this.domainRelRepository = domainRelRepository;
        this.domainHierarchyService = domainHierarchyService;
    }

    @Transactional(readOnly = true)
    public OntologyGraphResponse graph(Long domainId) {
        List<OntologyEntity> ontologies;
        if (domainId == null) {
            ontologies = ontologyRepository.findByStatusOrderByNameAsc(ConfigStatus.ENABLED);
        } else {
            Set<Long> domainIds = domainHierarchyService.getSelfAndDescendantIds(domainId);
            Set<Long> ids = new LinkedHashSet<>(domainIds.stream()
                    .flatMap(id -> domainRelRepository.findByDomainId(id).stream())
                    .map(DomainOntologyRelEntity::getOntologyId)
                    .toList());
            ontologies = ontologyRepository.findByIdIn(ids).stream()
                    .filter(entity -> entity.getStatus() == ConfigStatus.ENABLED)
                    .toList();
        }

        Set<Long> ontologyIds = new LinkedHashSet<>(ontologies.stream().map(OntologyEntity::getId).toList());
        List<OntologyRelationEntity> relations = ontologyIds.isEmpty()
                ? List.of()
                : relationRepository.findByStatusAndSourceOntologyIdInAndTargetOntologyIdIn(
                        ConfigStatus.ENABLED, ontologyIds, ontologyIds);

        List<OntologyGraphResponse.Node> nodes = ontologies.stream()
                .map(entity -> new OntologyGraphResponse.Node(entity.getId(), entity.getName(), entity.getCode()))
                .toList();
        List<OntologyGraphResponse.Edge> edges = relations.stream()
                .map(relation -> new OntologyGraphResponse.Edge(
                        relation.getId(),
                        relation.getSourceOntologyId(),
                        relation.getTargetOntologyId(),
                        relation.getName(),
                        relation.getCode(),
                        relation.getCardinality().name()
                ))
                .toList();
        return new OntologyGraphResponse(nodes, edges);
    }
}
