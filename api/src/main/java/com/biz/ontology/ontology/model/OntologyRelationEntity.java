package com.biz.ontology.ontology.model;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.persistence.VersionedEntity;
import com.biz.ontology.ontology.enums.RelationCardinality;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "ontology_relation", indexes = {
        @Index(name = "idx_relation_source_ontology", columnList = "source_ontology_id"),
        @Index(name = "idx_relation_target_ontology", columnList = "target_ontology_id"),
        @Index(name = "idx_relation_source_property", columnList = "source_property_id"),
        @Index(name = "idx_relation_target_property", columnList = "target_property_id")
})
public class OntologyRelationEntity extends VersionedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_ontology_id", nullable = false)
    private Long sourceOntologyId;

    @Column(name = "target_ontology_id", nullable = false)
    private Long targetOntologyId;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "cardinality", nullable = false, length = 20)
    private RelationCardinality cardinality;

    @Column(name = "source_property_id")
    private Long sourcePropertyId;

    @Column(name = "target_property_id")
    private Long targetPropertyId;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConfigStatus status = ConfigStatus.ENABLED;

    public Long getId() { return id; }
    public Long getSourceOntologyId() { return sourceOntologyId; }
    public void setSourceOntologyId(Long sourceOntologyId) { this.sourceOntologyId = sourceOntologyId; }
    public Long getTargetOntologyId() { return targetOntologyId; }
    public void setTargetOntologyId(Long targetOntologyId) { this.targetOntologyId = targetOntologyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public RelationCardinality getCardinality() { return cardinality; }
    public void setCardinality(RelationCardinality cardinality) { this.cardinality = cardinality; }
    public Long getSourcePropertyId() { return sourcePropertyId; }
    public void setSourcePropertyId(Long sourcePropertyId) { this.sourcePropertyId = sourcePropertyId; }
    public Long getTargetPropertyId() { return targetPropertyId; }
    public void setTargetPropertyId(Long targetPropertyId) { this.targetPropertyId = targetPropertyId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ConfigStatus getStatus() { return status; }
    public void setStatus(ConfigStatus status) { this.status = status; }
}
