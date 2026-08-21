package com.biz.ontology.ontology.repository;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.ontology.model.OntologyPropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OntologyPropertyRepository extends JpaRepository<OntologyPropertyEntity, Long> {
    List<OntologyPropertyEntity> findByOntologyIdOrderBySortOrderAscIdAsc(Long ontologyId);
    List<OntologyPropertyEntity> findByOntologyIdAndStatusOrderBySortOrderAscIdAsc(Long ontologyId, ConfigStatus status);
    boolean existsByOntologyId(Long ontologyId);
    long countByOntologyId(Long ontologyId);
    boolean existsByOntologyIdAndCodeIgnoreCase(Long ontologyId, String code);
    boolean existsByOntologyIdAndCodeIgnoreCaseAndIdNot(Long ontologyId, String code, Long id);
    boolean existsByOntologyIdAndUniqueFlagTrue(Long ontologyId);
    boolean existsByOntologyIdAndUniqueFlagTrueAndIdNot(Long ontologyId, Long id);
}
