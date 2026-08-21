package com.biz.ontology.rule.model;

import com.biz.ontology.rule.enums.RuleActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rule_action")
public class RuleActionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_version_id", nullable = false, unique = true)
    private Long ruleVersionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private RuleActionType actionType = RuleActionType.RETURN_RESULT;

    @Column(name = "result_code", nullable = false, length = 100)
    private String resultCode;

    @Column(name = "result_name", nullable = false, length = 100)
    private String resultName;

    @Column(name = "message", length = 1000)
    private String message;

    public Long getId() { return id; }
    public Long getRuleVersionId() { return ruleVersionId; }
    public void setRuleVersionId(Long ruleVersionId) { this.ruleVersionId = ruleVersionId; }
    public RuleActionType getActionType() { return actionType; }
    public void setActionType(RuleActionType actionType) { this.actionType = actionType; }
    public String getResultCode() { return resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }
    public String getResultName() { return resultName; }
    public void setResultName(String resultName) { this.resultName = resultName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
