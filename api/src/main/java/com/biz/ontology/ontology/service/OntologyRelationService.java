package com.biz.ontology.ontology.service;

import com.biz.ontology.api.ontology.dto.OntologyRelationResponse;
import com.biz.ontology.api.ontology.dto.SaveOntologyRelationRequest;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.ontology.enums.PropertyDataType;
import com.biz.ontology.ontology.model.OntologyPropertyEntity;
import com.biz.ontology.ontology.model.OntologyRelationEntity;
import com.biz.ontology.ontology.repository.OntologyPropertyRepository;
import com.biz.ontology.ontology.repository.OntologyRelationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class OntologyRelationService {

    private static final Set<PropertyDataType> NUMBER_TYPES = Set.of(
            PropertyDataType.INTEGER,
            PropertyDataType.DECIMAL
    );

    private final OntologyService ontologyService;
    private final OntologyPropertyRepository propertyRepository;
    private final OntologyRelationRepository relationRepository;

    public OntologyRelationService(
            OntologyService ontologyService,
            OntologyPropertyRepository propertyRepository,
            OntologyRelationRepository relationRepository) {
        this.ontologyService = ontologyService;
        this.propertyRepository = propertyRepository;
        this.relationRepository = relationRepository;
    }

    @Transactional(readOnly = true)
    public List<OntologyRelationResponse> list(Long sourceOntologyId) {
        ontologyService.findEntity(sourceOntologyId);
        return relationRepository.findBySourceOntologyIdOrderByUpdatedAtDesc(sourceOntologyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OntologyRelationResponse create(Long sourceOntologyId, SaveOntologyRelationRequest request) {
        ontologyService.findEntity(sourceOntologyId);
        ontologyService.findEntity(request.getTargetOntologyId());
        ensureCodeUnique(normalizeCode(request.getCode()), null);
        validateProperties(sourceOntologyId, request);

        OntologyRelationEntity entity = new OntologyRelationEntity();
        entity.setSourceOntologyId(sourceOntologyId);
        apply(entity, request);
        return toResponse(relationRepository.saveAndFlush(entity));
    }

    @Transactional
    public OntologyRelationResponse update(
            Long sourceOntologyId,
            Long relationId,
            SaveOntologyRelationRequest request) {
        OntologyRelationEntity entity = findRelation(sourceOntologyId, relationId);
        if (request.getVersion() == null || !Objects.equals(entity.getVersion(), request.getVersion())) {
            throw new BusinessException(PlatformErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
        ontologyService.findEntity(request.getTargetOntologyId());
        ensureCodeUnique(normalizeCode(request.getCode()), relationId);
        validateProperties(sourceOntologyId, request);
        apply(entity, request);
        return toResponse(relationRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(Long sourceOntologyId, Long relationId) {
        relationRepository.delete(findRelation(sourceOntologyId, relationId));
    }

    private OntologyRelationEntity findRelation(Long sourceOntologyId, Long relationId) {
        return relationRepository.findById(relationId)
                .filter(relation -> Objects.equals(relation.getSourceOntologyId(), sourceOntologyId))
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.RELATION_NOT_FOUND));
    }

    private void validateProperties(Long sourceOntologyId, SaveOntologyRelationRequest request) {
        boolean sourceMissing = request.getSourcePropertyId() == null;
        boolean targetMissing = request.getTargetPropertyId() == null;
        if (sourceMissing != targetMissing) {
            throw mismatch("起点属性和目标属性必须同时填写或同时为空");
        }
        if (sourceMissing) {
            return;
        }

        OntologyPropertyEntity sourceProperty = propertyRepository.findById(request.getSourcePropertyId())
                .filter(property -> Objects.equals(property.getOntologyId(), sourceOntologyId))
                .orElseThrow(() -> mismatch("起点属性不属于起点本体"));
        OntologyPropertyEntity targetProperty = propertyRepository.findById(request.getTargetPropertyId())
                .filter(property -> Objects.equals(property.getOntologyId(), request.getTargetOntologyId()))
                .orElseThrow(() -> mismatch("目标属性不属于目标本体"));

        if (!isComparable(sourceProperty.getDataType(), targetProperty.getDataType())) {
            throw mismatch("起点属性和目标属性的数据类型不可比较");
        }
    }

    private boolean isComparable(PropertyDataType source, PropertyDataType target) {
        return source == target || (NUMBER_TYPES.contains(source) && NUMBER_TYPES.contains(target));
    }

    private BusinessException mismatch(String message) {
        return new BusinessException(PlatformErrorCode.RELATION_PROPERTY_MISMATCH, message);
    }

    private void ensureCodeUnique(String code, Long currentId) {
        boolean exists = currentId == null
                ? relationRepository.existsByCodeIgnoreCase(code)
                : relationRepository.existsByCodeIgnoreCaseAndIdNot(code, currentId);
        if (exists) {
            throw new BusinessException(PlatformErrorCode.RELATION_CODE_EXISTS);
        }
    }

    private void apply(OntologyRelationEntity entity, SaveOntologyRelationRequest request) {
        entity.setTargetOntologyId(request.getTargetOntologyId());
        entity.setName(request.getName().trim());
        entity.setCode(normalizeCode(request.getCode()));
        entity.setCardinality(request.getCardinality());
        entity.setSourcePropertyId(request.getSourcePropertyId());
        entity.setTargetPropertyId(request.getTargetPropertyId());
        entity.setDescription(normalizeText(request.getDescription()));
        entity.setStatus(Objects.requireNonNullElse(request.getStatus(), ConfigStatus.ENABLED));
    }

    private OntologyRelationResponse toResponse(OntologyRelationEntity entity) {
        return new OntologyRelationResponse(
                entity.getId(), entity.getSourceOntologyId(), entity.getTargetOntologyId(), entity.getName(),
                entity.getCode(), entity.getCardinality(), entity.getSourcePropertyId(), entity.getTargetPropertyId(),
                entity.getDescription(), entity.getStatus(), entity.getVersion(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    private String normalizeCode(String code) { return code.trim().toUpperCase(Locale.ROOT); }
    private String normalizeText(String text) { return StringUtils.hasText(text) ? text.trim() : null; }
}
