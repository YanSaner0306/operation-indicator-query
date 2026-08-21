package com.biz.ontology.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public abstract class VersionedEntity extends AuditableEntity {

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public Long getVersion() {
        return version;
    }
}
