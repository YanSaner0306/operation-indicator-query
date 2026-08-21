package com.biz.ontology.ontology.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "domain_ontology_rel", indexes = {
        @Index(name = "idx_domain_ontology_domain", columnList = "domain_id"),
        @Index(name = "idx_domain_ontology_ontology", columnList = "ontology_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_domain_ontology", columnNames = {"domain_id", "ontology_id"})
})
public class DomainOntologyRelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "domain_id", nullable = false)
    private Long domainId;

    @Column(name = "ontology_id", nullable = false)
    private Long ontologyId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void initializeCreatedAt() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public Long getOntologyId() {
        return ontologyId;
    }

    public void setOntologyId(Long ontologyId) {
        this.ontologyId = ontologyId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
