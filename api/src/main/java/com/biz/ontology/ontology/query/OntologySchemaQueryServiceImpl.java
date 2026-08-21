package com.biz.ontology.ontology.query;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.domain.service.DomainHierarchyService;
import com.biz.ontology.ontology.enums.PropertyDataType;
import com.biz.ontology.ontology.model.DomainOntologyRelEntity;
import com.biz.ontology.ontology.model.OntologyEntity;
import com.biz.ontology.ontology.model.OntologyPropertyEntity;
import com.biz.ontology.ontology.repository.DomainOntologyRelRepository;
import com.biz.ontology.ontology.repository.OntologyPropertyRepository;
import com.biz.ontology.ontology.repository.OntologyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class OntologySchemaQueryServiceImpl implements OntologySchemaQueryService {

    private final OntologyRepository ontologyRepository;
    private final OntologyPropertyRepository propertyRepository;
    private final DomainOntologyRelRepository domainRelRepository;
    private final DomainHierarchyService domainHierarchyService;

    public OntologySchemaQueryServiceImpl(
            OntologyRepository ontologyRepository,
            OntologyPropertyRepository propertyRepository,
            DomainOntologyRelRepository domainRelRepository,
            DomainHierarchyService domainHierarchyService) {
        this.ontologyRepository = ontologyRepository;
        this.propertyRepository = propertyRepository;
        this.domainRelRepository = domainRelRepository;
        this.domainHierarchyService = domainHierarchyService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OntologySummary> listEnabledOntologies(Long domainId) {
        List<OntologyEntity> enabled = ontologyRepository.findByStatusOrderByNameAsc(ConfigStatus.ENABLED);
        if (domainId != null) {
            Set<Long> domainIds = domainHierarchyService.getSelfAndDescendantIds(domainId);
            Set<Long> ids = new HashSet<>(domainIds.stream()
                    .flatMap(id -> domainRelRepository.findByDomainId(id).stream())
                    .map(DomainOntologyRelEntity::getOntologyId)
                    .toList());
            enabled = enabled.stream().filter(entity -> ids.contains(entity.getId())).toList();
        }
        return enabled.stream()
                .map(entity -> new OntologySummary(entity.getId(), entity.getName(), entity.getCode()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OntologySchema getOntologySchema(Long ontologyId) {
        OntologyEntity ontology = findOntology(ontologyId);
        List<PropertyDefinition> properties = propertyRepository
                .findByOntologyIdOrderBySortOrderAscIdAsc(ontologyId).stream()
                .map(this::toDefinition)
                .toList();
        PropertyDefinition uniqueProperty = properties.stream()
                .filter(PropertyDefinition::uniqueFlag)
                .findFirst()
                .orElse(null);
        return new OntologySchema(ontology.getId(), ontology.getName(), ontology.getCode(), properties, uniqueProperty);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyDefinition> listBindableProperties(Long ontologyId) {
        OntologyEntity ontology = findOntology(ontologyId);
        if (ontology.getStatus() != ConfigStatus.ENABLED) {
            throw new BusinessException(PlatformErrorCode.ONTOLOGY_DISABLED);
        }
        return propertyRepository.findByOntologyIdAndStatusOrderBySortOrderAscIdAsc(
                        ontologyId, ConfigStatus.ENABLED).stream()
                .map(this::toDefinition)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MappingValidationResult validatePropertyMapping(Long propertyId, String sourceFieldType) {
        OntologyPropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.PROPERTY_NOT_FOUND));
        boolean valid = isCompatible(property.getDataType(), sourceFieldType);
        return new MappingValidationResult(
                valid,
                valid ? "ok" : "源字段类型与本体属性类型不兼容"
        );
    }

    private boolean isCompatible(PropertyDataType dataType, String sourceFieldType) {
        String type = sourceFieldType == null ? "" : sourceFieldType.toUpperCase(Locale.ROOT);
        return switch (dataType) {
            case STRING, ENUM -> type.contains("CHAR") || type.contains("TEXT") || type.contains("STRING");
            case INTEGER -> type.contains("INT") || type.contains("LONG") || type.contains("SHORT");
            case DECIMAL -> type.contains("DECIMAL") || type.contains("NUMERIC")
                    || type.contains("FLOAT") || type.contains("DOUBLE");
            case BOOLEAN -> type.contains("BOOL") || type.contains("BIT") || type.contains("TINYINT");
            case DATE -> type.equals("DATE");
            case DATETIME -> type.contains("DATETIME") || type.contains("TIMESTAMP");
        };
    }

    private OntologyEntity findOntology(Long id) {
        return ontologyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.ONTOLOGY_NOT_FOUND));
    }

    private PropertyDefinition toDefinition(OntologyPropertyEntity property) {
        return new PropertyDefinition(
                property.getId(), property.getName(), property.getCode(), property.getDataType(),
                property.isRequiredFlag(), property.isUniqueFlag()
        );
    }
}
