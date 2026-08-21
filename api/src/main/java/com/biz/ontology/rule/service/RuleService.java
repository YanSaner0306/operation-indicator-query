package com.biz.ontology.rule.service;

import com.biz.ontology.api.rule.dto.CreateRuleRequest;
import com.biz.ontology.api.rule.dto.RuleActionRequest;
import com.biz.ontology.api.rule.dto.RuleActionResponse;
import com.biz.ontology.api.rule.dto.RuleConditionRequest;
import com.biz.ontology.api.rule.dto.RuleConditionResponse;
import com.biz.ontology.api.rule.dto.RuleResponse;
import com.biz.ontology.api.rule.dto.RuleVersionResponse;
import com.biz.ontology.api.rule.dto.UpdateRuleRequest;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.ontology.model.OntologyEntity;
import com.biz.ontology.ontology.model.OntologyPropertyEntity;
import com.biz.ontology.ontology.repository.OntologyPropertyRepository;
import com.biz.ontology.ontology.repository.OntologyRepository;
import com.biz.ontology.rule.enums.RuleActionType;
import com.biz.ontology.rule.model.RuleActionEntity;
import com.biz.ontology.rule.model.RuleConditionEntity;
import com.biz.ontology.rule.model.RuleDefinitionEntity;
import com.biz.ontology.rule.model.RuleVersionEntity;
import com.biz.ontology.rule.repository.RuleActionRepository;
import com.biz.ontology.rule.repository.RuleConditionRepository;
import com.biz.ontology.rule.repository.RuleDefinitionRepository;
import com.biz.ontology.rule.repository.RuleVersionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class RuleService {

    private final RuleDefinitionRepository ruleRepository;
    private final RuleVersionRepository versionRepository;
    private final RuleConditionRepository conditionRepository;
    private final RuleActionRepository actionRepository;
    private final OntologyRepository ontologyRepository;
    private final OntologyPropertyRepository propertyRepository;
    private final RuleValueService ruleValueService;
    private final RuleExternalReferenceChecker externalReferenceChecker;

    public RuleService(
            RuleDefinitionRepository ruleRepository,
            RuleVersionRepository versionRepository,
            RuleConditionRepository conditionRepository,
            RuleActionRepository actionRepository,
            OntologyRepository ontologyRepository,
            OntologyPropertyRepository propertyRepository,
            RuleValueService ruleValueService,
            RuleExternalReferenceChecker externalReferenceChecker) {
        this.ruleRepository = ruleRepository;
        this.versionRepository = versionRepository;
        this.conditionRepository = conditionRepository;
        this.actionRepository = actionRepository;
        this.ontologyRepository = ontologyRepository;
        this.propertyRepository = propertyRepository;
        this.ruleValueService = ruleValueService;
        this.externalReferenceChecker = externalReferenceChecker;
    }

    @Transactional
    public RuleResponse create(CreateRuleRequest request) {
        String code = normalizeCode(request.getCode());
        ensureCodeUnique(code, null);
        OntologyEntity ontology = findEnabledOntology(request.getOntologyId());
        ValidatedCondition validated = validateCondition(ontology.getId(), request.getCondition());

        RuleDefinitionEntity definition = new RuleDefinitionEntity();
        definition.setName(request.getName().trim());
        definition.setCode(code);
        definition.setOntologyId(ontology.getId());
        definition.setDescription(normalizeText(request.getDescription()));
        definition.setEnabledFlag(request.isEnabled());
        definition.setDeletedFlag(false);
        definition = ruleRepository.saveAndFlush(definition);

        RuleVersionEntity version = createVersion(
                definition.getId(),
                1,
                request.getChangeNote(),
                request.getCreatedBy(),
                validated,
                request.getAction()
        );
        definition.setCurrentVersionId(version.getId());
        return toResponse(ruleRepository.saveAndFlush(definition));
    }

    @Transactional(readOnly = true)
    public Page<RuleResponse> page(String keyword, Long ontologyId, Boolean enabled, Pageable pageable) {
        Specification<RuleDefinitionEntity> specification = (root, query, builder) ->
                builder.isFalse(root.get("deletedFlag"));
        if (StringUtils.hasText(keyword)) {
            String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("name")), pattern),
                    builder.like(builder.lower(root.get("code")), pattern)
            ));
        }
        if (ontologyId != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("ontologyId"), ontologyId));
        }
        if (enabled != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("enabledFlag"), enabled));
        }
        return ruleRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RuleResponse getById(Long id) {
        return toResponse(findRule(id));
    }

    @Transactional
    public RuleResponse update(Long id, UpdateRuleRequest request) {
        RuleDefinitionEntity definition = findRule(id);
        ensureVersion(definition, request.getVersion());
        ensureCodeUnique(normalizeCode(request.getCode()), id);
        findEnabledOntology(definition.getOntologyId());
        ValidatedCondition validated = validateCondition(definition.getOntologyId(), request.getCondition());

        int nextVersionNo = versionRepository.findTopByRuleIdOrderByVersionNoDesc(id)
                .map(version -> version.getVersionNo() + 1)
                .orElse(1);
        RuleVersionEntity version = createVersion(
                id,
                nextVersionNo,
                request.getChangeNote(),
                request.getCreatedBy(),
                validated,
                request.getAction()
        );

        definition.setName(request.getName().trim());
        definition.setCode(normalizeCode(request.getCode()));
        definition.setDescription(normalizeText(request.getDescription()));
        definition.setCurrentVersionId(version.getId());
        return toResponse(ruleRepository.saveAndFlush(definition));
    }

    @Transactional
    public RuleResponse updateEnabled(Long id, boolean enabled, Long version) {
        RuleDefinitionEntity definition = findRule(id);
        if (version != null) {
            ensureVersion(definition, version);
        }
        if (enabled) {
            findEnabledOntology(definition.getOntologyId());
        }
        definition.setEnabledFlag(enabled);
        return toResponse(ruleRepository.saveAndFlush(definition));
    }

    @Transactional
    public void delete(Long id) {
        RuleDefinitionEntity definition = findRule(id);
        if (definition.isEnabledFlag() || externalReferenceChecker.isRuleReferenced(id)) {
            throw new BusinessException(PlatformErrorCode.RULE_DELETE_FORBIDDEN, "请先禁用规则并解除外部引用");
        }
        definition.setDeletedFlag(true);
        ruleRepository.saveAndFlush(definition);
    }

    @Transactional(readOnly = true)
    public List<RuleVersionResponse> listVersions(Long ruleId) {
        findRule(ruleId);
        return versionRepository.findByRuleIdOrderByVersionNoDesc(ruleId).stream()
                .map(this::toVersionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RuleVersionResponse getVersion(Long ruleId, Long versionId) {
        findRule(ruleId);
        return toVersionResponse(findVersion(ruleId, versionId));
    }

    @Transactional
    public RuleResponse switchVersion(Long ruleId, Long versionId, Long definitionVersion) {
        RuleDefinitionEntity definition = findRule(ruleId);
        if (definitionVersion != null) {
            ensureVersion(definition, definitionVersion);
        }
        RuleVersionEntity version = findVersion(ruleId, versionId);
        definition.setCurrentVersionId(version.getId());
        return toResponse(ruleRepository.saveAndFlush(definition));
    }

    public RuleDefinitionEntity findRule(Long id) {
        RuleDefinitionEntity entity = ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.RULE_NOT_FOUND));
        if (entity.isDeletedFlag()) {
            throw new BusinessException(PlatformErrorCode.RULE_NOT_FOUND);
        }
        return entity;
    }

    public RuleVersionEntity findVersion(Long ruleId, Long versionId) {
        return versionRepository.findByIdAndRuleId(versionId, ruleId)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.RULE_VERSION_NOT_FOUND));
    }

    private RuleVersionEntity createVersion(
            Long ruleId,
            int versionNo,
            String changeNote,
            String createdBy,
            ValidatedCondition validated,
            RuleActionRequest actionRequest) {
        RuleVersionEntity version = new RuleVersionEntity();
        version.setRuleId(ruleId);
        version.setVersionNo(versionNo);
        version.setChangeNote(normalizeText(changeNote));
        version.setCreatedBy(normalizeText(createdBy));
        version = versionRepository.saveAndFlush(version);

        RuleConditionEntity condition = new RuleConditionEntity();
        condition.setRuleVersionId(version.getId());
        condition.setOntologyId(validated.property().getOntologyId());
        condition.setPropertyId(validated.property().getId());
        condition.setOperator(validated.request().getOperator());
        condition.setCompareValue(validated.normalizedCompareValue());
        condition.setValueType(validated.property().getDataType());
        conditionRepository.save(condition);

        RuleActionEntity action = new RuleActionEntity();
        action.setRuleVersionId(version.getId());
        action.setActionType(RuleActionType.RETURN_RESULT);
        action.setResultCode(normalizeCode(actionRequest.getResultCode()));
        action.setResultName(actionRequest.getResultName().trim());
        action.setMessage(normalizeText(actionRequest.getMessage()));
        actionRepository.save(action);
        return version;
    }

    private ValidatedCondition validateCondition(Long ontologyId, RuleConditionRequest request) {
        OntologyPropertyEntity property = propertyRepository.findById(request.getPropertyId())
                .filter(candidate -> Objects.equals(candidate.getOntologyId(), ontologyId))
                .filter(candidate -> candidate.getStatus() == ConfigStatus.ENABLED)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.RULE_PROPERTY_NOT_FOUND));
        String compareValue = ruleValueService.normalizeCompareValue(
                property.getDataType(),
                request.getOperator(),
                request.getCompareValue()
        );
        return new ValidatedCondition(property, request, compareValue);
    }

    private OntologyEntity findEnabledOntology(Long ontologyId) {
        OntologyEntity ontology = ontologyRepository.findById(ontologyId)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.ONTOLOGY_NOT_FOUND));
        if (ontology.getStatus() != ConfigStatus.ENABLED) {
            throw new BusinessException(PlatformErrorCode.RULE_ONTOLOGY_DISABLED);
        }
        return ontology;
    }

    private void ensureCodeUnique(String code, Long currentId) {
        boolean exists = currentId == null
                ? ruleRepository.existsByCodeIgnoreCase(code)
                : ruleRepository.existsByCodeIgnoreCaseAndIdNot(code, currentId);
        if (exists) {
            throw new BusinessException(PlatformErrorCode.RULE_CODE_EXISTS);
        }
    }

    private void ensureVersion(RuleDefinitionEntity definition, Long requestedVersion) {
        if (!Objects.equals(definition.getVersion(), requestedVersion)) {
            throw new BusinessException(PlatformErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
    }

    private RuleResponse toResponse(RuleDefinitionEntity definition) {
        OntologyEntity ontology = ontologyRepository.findById(definition.getOntologyId())
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.ONTOLOGY_NOT_FOUND));
        RuleVersionEntity version = findVersion(definition.getId(), definition.getCurrentVersionId());
        RuleConditionResponse condition = toConditionResponse(version.getId());
        RuleActionResponse action = toActionResponse(version.getId());
        return new RuleResponse(
                definition.getId(),
                definition.getName(),
                definition.getCode(),
                definition.getOntologyId(),
                ontology.getName(),
                definition.getDescription(),
                definition.isEnabledFlag(),
                definition.getCurrentVersionId(),
                version.getVersionNo(),
                condition,
                action,
                definition.getVersion(),
                definition.getCreatedAt(),
                definition.getUpdatedAt()
        );
    }

    private RuleVersionResponse toVersionResponse(RuleVersionEntity version) {
        return new RuleVersionResponse(
                version.getId(),
                version.getVersionNo(),
                version.getChangeNote(),
                version.getCreatedBy(),
                version.getCreatedAt(),
                toConditionResponse(version.getId()),
                toActionResponse(version.getId())
        );
    }

    private RuleConditionResponse toConditionResponse(Long versionId) {
        RuleConditionEntity condition = conditionRepository.findByRuleVersionId(versionId)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.RULE_VERSION_NOT_FOUND));
        OntologyPropertyEntity property = propertyRepository.findById(condition.getPropertyId())
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.RULE_PROPERTY_NOT_FOUND));
        return new RuleConditionResponse(
                property.getId(),
                property.getName(),
                condition.getOperator(),
                condition.getCompareValue(),
                condition.getValueType()
        );
    }

    private RuleActionResponse toActionResponse(Long versionId) {
        RuleActionEntity action = actionRepository.findByRuleVersionId(versionId)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.RULE_VERSION_NOT_FOUND));
        return new RuleActionResponse(
                action.getActionType(),
                action.getResultCode(),
                action.getResultName(),
                action.getMessage()
        );
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private record ValidatedCondition(
            OntologyPropertyEntity property,
            RuleConditionRequest request,
            String normalizedCompareValue
    ) {
    }
}
