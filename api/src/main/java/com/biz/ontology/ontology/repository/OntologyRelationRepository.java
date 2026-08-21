package com.biz.ontology.ontology.repository;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.ontology.model.OntologyRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OntologyRelationRepository extends JpaRepository<OntologyRelationEntity, Long> {
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    boolean existsBySourceOntologyIdOrTargetOntologyId(Long sourceOntologyId, Long targetOntologyId);
    boolean existsBySourcePropertyIdOrTargetPropertyId(Long sourcePropertyId, Long targetPropertyId);
    long countBySourceOntologyIdOrTargetOntologyId(Long sourceOntologyId, Long targetOntologyId);
    List<OntologyRelationEntity> findBySourceOntologyIdOrderByUpdatedAtDesc(Long sourceOntologyId);
    List<OntologyRelationEntity> findByStatus(ConfigStatus status);
    List<OntologyRelationEntity> findByStatusAndSourceOntologyIdInAndTargetOntologyIdIn(
            ConfigStatus status,
            Collection<Long> sourceOntologyIds,
            Collection<Long> targetOntologyIds);
}
