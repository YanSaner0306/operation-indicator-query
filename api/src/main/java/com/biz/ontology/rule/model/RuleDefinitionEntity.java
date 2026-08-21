package com.biz.ontology.rule.model;

import com.biz.ontology.common.persistence.VersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "rule_definition", indexes = {
        @Index(name = "idx_rule_ontology", columnList = "ontology_id"),
        @Index(name = "idx_rule_current_version", columnList = "current_version_id"),
        @Index(name = "idx_rule_enabled_deleted", columnList = "enabled_flag,deleted_flag")
})
public class RuleDefinitionEntity extends VersionedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "ontology_id", nullable = false)
    private Long ontologyId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "current_version_id")
    private Long currentVersionId;

    @Column(name = "enabled_flag", nullable = false)
    private boolean enabledFlag;

    @Column(name = "deleted_flag", nullable = false)
    private boolean deletedFlag;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getOntologyId() { return ontologyId; }
    public void setOntologyId(Long ontologyId) { this.ontologyId = ontologyId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCurrentVersionId() { return currentVersionId; }
    public void setCurrentVersionId(Long currentVersionId) { this.currentVersionId = currentVersionId; }
    public boolean isEnabledFlag() { return enabledFlag; }
    public void setEnabledFlag(boolean enabledFlag) { this.enabledFlag = enabledFlag; }
    public boolean isDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(boolean deletedFlag) { this.deletedFlag = deletedFlag; }
}
