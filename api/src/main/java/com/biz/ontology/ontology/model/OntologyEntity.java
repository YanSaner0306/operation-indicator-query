package com.biz.ontology.ontology.model;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.persistence.VersionedEntity;
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
@Table(name = "ontology", indexes = {
        @Index(name = "idx_ontology_status", columnList = "status"),
        @Index(name = "idx_ontology_updated_at", columnList = "updated_at")
})
public class OntologyEntity extends VersionedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConfigStatus status = ConfigStatus.ENABLED;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ConfigStatus getStatus() {
        return status;
    }

    public void setStatus(ConfigStatus status) {
        this.status = status;
    }
}
