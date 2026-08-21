package com.biz.ontology.ontology.repository;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.ontology.model.OntologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface OntologyRepository extends JpaRepository<OntologyEntity, Long>, JpaSpecificationExecutor<OntologyEntity> {
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    List<OntologyEntity> findByIdIn(Collection<Long> ids);
    List<OntologyEntity> findByStatusOrderByNameAsc(ConfigStatus status);
}
