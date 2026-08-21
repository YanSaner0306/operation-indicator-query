package com.biz.ontology.rule.service;

import com.biz.ontology.api.rule.dto.RuleActionResponse;
import com.biz.ontology.api.rule.dto.RuleTestRequest;
import com.biz.ontology.api.rule.dto.RuleTestResponse;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.ontology.model.OntologyPropertyEntity;
import com.biz.ontology.ontology.repository.OntologyPropertyRepository;
import com.biz.ontology.rule.model.RuleActionEntity;
import com.biz.ontology.rule.model.RuleConditionEntity;
import com.biz.ontology.rule.model.RuleDefinitionEntity;
import com.biz.ontology.rule.model.RuleVersionEntity;
import com.biz.ontology.rule.repository.RuleActionRepository;
import com.biz.ontology.rule.repository.RuleConditionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuleTestService {

    private final RuleService ruleService;
    private final RuleConditionRepository conditionRepository;
    private final RuleActionRepository actionRepository;
    private final OntologyPropertyRepository propertyRepository;
    private final RuleValueService ruleValueService;

    public RuleTestService(
            RuleService ruleService,
            RuleConditionRepository conditionRepository,
            RuleActionRepository actionRepository,
            OntologyPropertyRepository propertyRepository,
            RuleValueService ruleValueService) {
        this.ruleService = ruleService;
        this.conditionRepository = conditionRepository;
        this.actionRepository = actionRepository;
        this.propertyRepository = propertyRepository;
        this.ruleValueService = ruleValueService;
    }

    @Transactional(readOnly = true)
    public RuleTestResponse test(Long ruleId, RuleTestRequest request) {
        RuleDefinitionEntity definition = ruleService.findRule(ruleId);
        Long versionId = request.getVersionId() == null
                ? definition.getCurrentVersionId()
                : request.getVersionId();
        RuleVersionEntity version = ruleService.findVersion(ruleId, versionId);
        RuleConditionEntity condition = conditionRepository.findByRuleVersionId(versionId)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.RULE_VERSION_NOT_FOUND));
        RuleActionEntity action = actionRepository.findByRuleVersionId(versionId)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.RULE_VERSION_NOT_FOUND));
        OntologyPropertyEntity property = propertyRepository.findById(condition.getPropertyId())
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.RULE_PROPERTY_NOT_FOUND));

        String propertyKey = condition.getPropertyId().toString();
        if (!request.getValues().containsKey(propertyKey)) {
            throw new BusinessException(PlatformErrorCode.RULE_VALUE_MISSING);
        }
        Object rawValue = request.getValues().get(propertyKey);
        Object actualValue = ruleValueService.normalizeActualValue(
                condition.getValueType(),
                condition.getOperator(),
                rawValue
        );
        boolean matched = ruleValueService.evaluate(
                condition.getValueType(),
                condition.getOperator(),
                actualValue,
                condition.getCompareValue()
        );

        RuleActionResponse actionResponse = matched
                ? new RuleActionResponse(
                        action.getActionType(),
                        action.getResultCode(),
                        action.getResultName(),
                        action.getMessage())
                : null;
        return new RuleTestResponse(
                definition.getId(),
                version.getVersionNo(),
                matched,
                new RuleTestResponse.ConditionProcess(
                        property.getId(),
                        property.getName(),
                        actualValue,
                        condition.getOperator(),
                        condition.getCompareValue()
                ),
                actionResponse
        );
    }
}
