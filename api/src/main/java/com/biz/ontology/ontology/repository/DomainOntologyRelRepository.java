package com.biz.ontology.ontology.repository;

import com.biz.ontology.ontology.model.DomainOntologyRelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DomainOntologyRelRepository extends JpaRepository<DomainOntologyRelEntity, Long> {

    boolean existsByDomainId(Long domainId);

    List<DomainOntologyRelEntity> findByOntologyId(Long ontologyId);

    List<DomainOntologyRelEntity> findByDomainId(Long domainId);

    List<DomainOntologyRelEntity> findByOntologyIdIn(Collection<Long> ontologyIds);

    void deleteByOntologyId(Long ontologyId);
}
