package com.biz.ontology.ontology.model;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.persistence.AuditableEntity;
import com.biz.ontology.ontology.enums.PropertyDataType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "ontology_property", indexes = {
        @Index(name = "idx_property_ontology", columnList = "ontology_id"),
        @Index(name = "idx_property_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_property_ontology_code", columnNames = {"ontology_id", "code"})
})
public class OntologyPropertyEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ontology_id", nullable = false)
    private Long ontologyId;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private PropertyDataType dataType;

    @Column(name = "length_value")
    private Integer lengthValue;

    @Column(name = "precision_value")
    private Integer precisionValue;

    @Column(name = "scale_value")
    private Integer scaleValue;

    @Column(name = "required_flag", nullable = false)
    private boolean requiredFlag;

    @Column(name = "unique_flag", nullable = false)
    private boolean uniqueFlag;

    @Column(name = "default_value", length = 500)
    private String defaultValue;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConfigStatus status = ConfigStatus.ENABLED;

    public Long getId() { return id; }
    public Long getOntologyId() { return ontologyId; }
    public void setOntologyId(Long ontologyId) { this.ontologyId = ontologyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public PropertyDataType getDataType() { return dataType; }
    public void setDataType(PropertyDataType dataType) { this.dataType = dataType; }
    public Integer getLengthValue() { return lengthValue; }
    public void setLengthValue(Integer lengthValue) { this.lengthValue = lengthValue; }
    public Integer getPrecisionValue() { return precisionValue; }
    public void setPrecisionValue(Integer precisionValue) { this.precisionValue = precisionValue; }
    public Integer getScaleValue() { return scaleValue; }
    public void setScaleValue(Integer scaleValue) { this.scaleValue = scaleValue; }
    public boolean isRequiredFlag() { return requiredFlag; }
    public void setRequiredFlag(boolean requiredFlag) { this.requiredFlag = requiredFlag; }
    public boolean isUniqueFlag() { return uniqueFlag; }
    public void setUniqueFlag(boolean uniqueFlag) { this.uniqueFlag = uniqueFlag; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public ConfigStatus getStatus() { return status; }
    public void setStatus(ConfigStatus status) { this.status = status; }
}
