package com.biz.ontology.rule.model;

import com.biz.ontology.ontology.enums.PropertyDataType;
import com.biz.ontology.rule.enums.RuleOperator;
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
@Table(name = "rule_condition", indexes = {
        @Index(name = "idx_rule_condition_property", columnList = "property_id")
})
public class RuleConditionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_version_id", nullable = false, unique = true)
    private Long ruleVersionId;

    @Column(name = "ontology_id", nullable = false)
    private Long ontologyId;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 30)
    private RuleOperator operator;

    @Column(name = "compare_value", length = 1000)
    private String compareValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    private PropertyDataType valueType;

    public Long getId() { return id; }
    public Long getRuleVersionId() { return ruleVersionId; }
    public void setRuleVersionId(Long ruleVersionId) { this.ruleVersionId = ruleVersionId; }
    public Long getOntologyId() { return ontologyId; }
    public void setOntologyId(Long ontologyId) { this.ontologyId = ontologyId; }
    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public RuleOperator getOperator() { return operator; }
    public void setOperator(RuleOperator operator) { this.operator = operator; }
    public String getCompareValue() { return compareValue; }
    public void setCompareValue(String compareValue) { this.compareValue = compareValue; }
    public PropertyDataType getValueType() { return valueType; }
    public void setValueType(PropertyDataType valueType) { this.valueType = valueType; }
}
