package com.biz.ontology.ontology.service;

import com.biz.ontology.api.ontology.dto.OntologyPropertyResponse;
import com.biz.ontology.api.ontology.dto.SaveOntologyPropertyRequest;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.ontology.enums.PropertyDataType;
import com.biz.ontology.ontology.model.OntologyPropertyEntity;
import com.biz.ontology.ontology.query.OntologyReferenceQueryService;
import com.biz.ontology.ontology.repository.OntologyPropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class OntologyPropertyService {

    private final OntologyService ontologyService;
    private final OntologyPropertyRepository propertyRepository;
    private final OntologyPropertyDefinitionValidator definitionValidator;
    private final OntologyReferenceQueryService referenceQueryService;

    public OntologyPropertyService(
            OntologyService ontologyService,
            OntologyPropertyRepository propertyRepository,
            OntologyPropertyDefinitionValidator definitionValidator,
            OntologyReferenceQueryService referenceQueryService) {
        this.ontologyService = ontologyService;
        this.propertyRepository = propertyRepository;
        this.definitionValidator = definitionValidator;
        this.referenceQueryService = referenceQueryService;
    }

    @Transactional(readOnly = true)
    public List<OntologyPropertyResponse> list(Long ontologyId) {
        ontologyService.findEntity(ontologyId);
        return propertyRepository.findByOntologyIdOrderBySortOrderAscIdAsc(ontologyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OntologyPropertyResponse create(Long ontologyId, SaveOntologyPropertyRequest request) {
        ontologyService.findEntity(ontologyId);
        definitionValidator.validate(request);
        ensureCodeUnique(ontologyId, normalizeCode(request.getCode()), null);
        ensureUniqueFlagAvailable(ontologyId, request.isUniqueFlag(), null);

        OntologyPropertyEntity entity = new OntologyPropertyEntity();
        entity.setOntologyId(ontologyId);
        apply(entity, request);
        return toResponse(propertyRepository.saveAndFlush(entity));
    }

    @Transactional
    public OntologyPropertyResponse update(
            Long ontologyId,
            Long propertyId,
            SaveOntologyPropertyRequest request) {
        OntologyPropertyEntity entity = findProperty(ontologyId, propertyId);
        definitionValidator.validate(request);
        ensureCodeUnique(ontologyId, normalizeCode(request.getCode()), propertyId);
        ensureUniqueFlagAvailable(ontologyId, request.isUniqueFlag(), propertyId);
        var references = referenceQueryService.getPropertyReferences(propertyId);
        if (entity.getDataType() != request.getDataType() && references.referenced()) {
            throw new BusinessException(PlatformErrorCode.PROPERTY_REFERENCED, "属性已被引用，不能直接修改数据类型");
        }
        if (entity.isUniqueFlag() && !request.isUniqueFlag() && references.bindingCount() > 0) {
            throw new BusinessException(PlatformErrorCode.PROPERTY_REFERENCED, "unique 属性已被 Binding 引用，不能取消 unique");
        }
        apply(entity, request);
        return toResponse(propertyRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(Long ontologyId, Long propertyId) {
        OntologyPropertyEntity entity = findProperty(ontologyId, propertyId);
        if (referenceQueryService.getPropertyReferences(propertyId).referenced()) {
            throw new BusinessException(PlatformErrorCode.PROPERTY_REFERENCED);
        }
        propertyRepository.delete(entity);
    }

    public OntologyPropertyEntity findProperty(Long ontologyId, Long propertyId) {
        return propertyRepository.findById(propertyId)
                .filter(property -> Objects.equals(property.getOntologyId(), ontologyId))
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.PROPERTY_NOT_FOUND));
    }

    private void apply(OntologyPropertyEntity entity, SaveOntologyPropertyRequest request) {
        entity.setName(request.getName().trim());
        entity.setCode(normalizeCode(request.getCode()));
        entity.setDataType(request.getDataType());
        entity.setLengthValue(request.getDataType() == PropertyDataType.STRING ? request.getLength() : null);
        entity.setPrecisionValue(request.getDataType() == PropertyDataType.DECIMAL ? request.getPrecision() : null);
        entity.setScaleValue(request.getDataType() == PropertyDataType.DECIMAL ? request.getScale() : null);
        entity.setRequiredFlag(request.isRequired());
        entity.setUniqueFlag(request.isUniqueFlag());
        entity.setDefaultValue(normalizeText(request.getDefaultValue()));
        entity.setDescription(normalizeText(request.getDescription()));
        entity.setSortOrder(Objects.requireNonNullElse(request.getSortOrder(), 0));
        entity.setStatus(Objects.requireNonNullElse(request.getStatus(), ConfigStatus.ENABLED));
    }

    private void ensureCodeUnique(Long ontologyId, String code, Long currentId) {
        boolean exists = currentId == null
                ? propertyRepository.existsByOntologyIdAndCodeIgnoreCase(ontologyId, code)
                : propertyRepository.existsByOntologyIdAndCodeIgnoreCaseAndIdNot(ontologyId, code, currentId);
        if (exists) {
            throw new BusinessException(PlatformErrorCode.PROPERTY_CODE_EXISTS);
        }
    }

    private void ensureUniqueFlagAvailable(Long ontologyId, boolean uniqueFlag, Long currentId) {
        if (!uniqueFlag) {
            return;
        }
        boolean exists = currentId == null
                ? propertyRepository.existsByOntologyIdAndUniqueFlagTrue(ontologyId)
                : propertyRepository.existsByOntologyIdAndUniqueFlagTrueAndIdNot(ontologyId, currentId);
        if (exists) {
            throw new BusinessException(PlatformErrorCode.PROPERTY_UNIQUE_CONFLICT);
        }
    }

    private OntologyPropertyResponse toResponse(OntologyPropertyEntity entity) {
        return new OntologyPropertyResponse(
                entity.getId(), entity.getOntologyId(), entity.getName(), entity.getCode(), entity.getDataType(),
                entity.getLengthValue(), entity.getPrecisionValue(), entity.getScaleValue(), entity.isRequiredFlag(),
                entity.isUniqueFlag(), entity.getDefaultValue(), entity.getDescription(), entity.getSortOrder(),
                entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    private String normalizeCode(String code) { return code.trim().toUpperCase(Locale.ROOT); }
    private String normalizeText(String text) { return StringUtils.hasText(text) ? text.trim() : null; }
}
