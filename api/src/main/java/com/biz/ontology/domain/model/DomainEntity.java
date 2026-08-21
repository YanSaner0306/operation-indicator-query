package com.biz.ontology.domain.model;

import com.biz.ontology.common.persistence.VersionedEntity;
import com.biz.ontology.domain.enums.DomainStatus;
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
@Table(name = "biz_domain", indexes = {
        @Index(name = "idx_biz_domain_parent_id", columnList = "parent_id"),
        @Index(name = "idx_biz_domain_status", columnList = "status"),
        @Index(name = "idx_biz_domain_sort_order", columnList = "sort_order")
})
public class DomainEntity extends VersionedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DomainStatus status = DomainStatus.ENABLED;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public DomainStatus getStatus() {
        return status;
    }

    public void setStatus(DomainStatus status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
