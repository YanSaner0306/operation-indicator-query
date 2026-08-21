package com.biz.ontology.ontology.service;

import com.biz.ontology.api.ontology.dto.CreateOntologyRequest;
import com.biz.ontology.api.ontology.dto.OntologyResponse;
import com.biz.ontology.api.ontology.dto.UpdateOntologyRequest;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.domain.enums.DomainStatus;
import com.biz.ontology.domain.model.DomainEntity;
import com.biz.ontology.domain.repository.DomainRepository;
import com.biz.ontology.domain.service.DomainHierarchyService;
import com.biz.ontology.ontology.model.DomainOntologyRelEntity;
import com.biz.ontology.ontology.model.OntologyEntity;
import com.biz.ontology.ontology.query.OntologyReferenceQueryService;
import com.biz.ontology.ontology.repository.DomainOntologyRelRepository;
import com.biz.ontology.ontology.repository.OntologyPropertyRepository;
import com.biz.ontology.ontology.repository.OntologyRelationRepository;
import com.biz.ontology.ontology.repository.OntologyRepository;
import com.biz.ontology.rule.repository.RuleDefinitionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class OntologyService {

    private final OntologyRepository ontologyRepository;
    private final DomainRepository domainRepository;
    private final DomainHierarchyService domainHierarchyService;
    private final DomainOntologyRelRepository domainRelRepository;
    private final OntologyPropertyRepository propertyRepository;
    private final OntologyRelationRepository relationRepository;
    private final RuleDefinitionRepository ruleRepository;
    private final OntologyReferenceQueryService referenceQueryService;

    public OntologyService(
            OntologyRepository ontologyRepository,
            DomainRepository domainRepository,
            DomainHierarchyService domainHierarchyService,
            DomainOntologyRelRepository domainRelRepository,
            OntologyPropertyRepository propertyRepository,
            OntologyRelationRepository relationRepository,
            RuleDefinitionRepository ruleRepository,
            OntologyReferenceQueryService referenceQueryService) {
        this.ontologyRepository = ontologyRepository;
        this.domainRepository = domainRepository;
        this.domainHierarchyService = domainHierarchyService;
        this.domainRelRepository = domainRelRepository;
        this.propertyRepository = propertyRepository;
        this.relationRepository = relationRepository;
        this.ruleRepository = ruleRepository;
        this.referenceQueryService = referenceQueryService;
    }

    @Transactional
    public OntologyResponse create(CreateOntologyRequest request) {
        String code = normalizeCode(request.getCode());
        ensureCodeUnique(code, null);
        Set<Long> domainIds = distinctIds(request.getDomainIds());
        validateDomains(domainIds, Set.of());

        OntologyEntity entity = new OntologyEntity();
        entity.setName(request.getName().trim());
        entity.setCode(code);
        entity.setDescription(normalizeText(request.getDescription()));
        entity.setStatus(Objects.requireNonNullElse(request.getStatus(), ConfigStatus.ENABLED));
        entity = ontologyRepository.saveAndFlush(entity);
        replaceDomainRelations(entity.getId(), domainIds);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<OntologyResponse> page(
            Long domainId,
            boolean unclassified,
            String keyword,
            ConfigStatus status,
            Pageable pageable) {
        Specification<OntologyEntity> specification = (root, query, builder) -> builder.conjunction();
        if (unclassified) {
            specification = specification.and((root, query, builder) -> {
                var relationIds = query.subquery(Long.class);
                var relation = relationIds.from(DomainOntologyRelEntity.class);
                relationIds.select(relation.get("ontologyId"));
                return builder.not(root.get("id").in(relationIds));
            });
        } else if (domainId != null) {
            Set<Long> domainIds = domainHierarchyService.getSelfAndDescendantIds(domainId);
            List<Long> ids = domainIds.stream()
                    .flatMap(id -> domainRelRepository.findByDomainId(id).stream())
                    .map(DomainOntologyRelEntity::getOntologyId)
                    .distinct()
                    .toList();
            specification = specification.and((root, query, builder) ->
                    ids.isEmpty() ? builder.disjunction() : root.get("id").in(ids));
        }
        if (StringUtils.hasText(keyword)) {
            String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("name")), pattern),
                    builder.like(builder.lower(root.get("code")), pattern)
            ));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        return ontologyRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OntologyResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public OntologyResponse update(Long id, UpdateOntologyRequest request) {
        OntologyEntity entity = findEntity(id);
        ensureVersion(entity, request.getVersion());
        ensureCodeUnique(normalizeCode(request.getCode()), id);

        Set<Long> existingDomainIds = getDomainIds(id);
        Set<Long> requestedDomainIds = distinctIds(request.getDomainIds());
        validateDomains(requestedDomainIds, existingDomainIds);

        entity.setName(request.getName().trim());
        entity.setCode(normalizeCode(request.getCode()));
        entity.setDescription(normalizeText(request.getDescription()));
        entity.setStatus(request.getStatus());
        entity = ontologyRepository.saveAndFlush(entity);
        replaceDomainRelations(id, requestedDomainIds);
        return toResponse(entity);
    }

    @Transactional
    public OntologyResponse updateStatus(Long id, ConfigStatus status, Long version) {
        OntologyEntity entity = findEntity(id);
        if (version != null) {
            ensureVersion(entity, version);
        }
        entity.setStatus(Objects.requireNonNull(status, "本体状态不能为空"));
        return toResponse(ontologyRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(Long id) {
        OntologyEntity entity = findEntity(id);
        if (referenceQueryService.getOntologyReferences(id).referenced()) {
            throw new BusinessException(PlatformErrorCode.ONTOLOGY_REFERENCED);
        }
        domainRelRepository.deleteByOntologyId(id);
        ontologyRepository.delete(entity);
    }

    public OntologyEntity findEntity(Long id) {
        return ontologyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.ONTOLOGY_NOT_FOUND));
    }

    private void replaceDomainRelations(Long ontologyId, Set<Long> domainIds) {
        domainRelRepository.deleteByOntologyId(ontologyId);
        domainRelRepository.flush();
        List<DomainOntologyRelEntity> relations = new ArrayList<>();
        for (Long domainId : domainIds) {
            DomainOntologyRelEntity relation = new DomainOntologyRelEntity();
            relation.setDomainId(domainId);
            relation.setOntologyId(ontologyId);
            relations.add(relation);
        }
        domainRelRepository.saveAll(relations);
    }

    private void validateDomains(Set<Long> requestedDomainIds, Set<Long> existingDomainIds) {
        if (requestedDomainIds.isEmpty()) {
            return;
        }
        List<DomainEntity> domains = domainRepository.findAllById(requestedDomainIds);
        if (domains.size() != requestedDomainIds.size()) {
            throw new BusinessException(PlatformErrorCode.DOMAIN_NOT_FOUND);
        }
        for (DomainEntity domain : domains) {
            if (!existingDomainIds.contains(domain.getId()) && domain.getStatus() != DomainStatus.ENABLED) {
                throw new BusinessException(PlatformErrorCode.DOMAIN_DISABLED, "禁用领域不能新增本体关联");
            }
        }
    }

    private Set<Long> distinctIds(List<Long> ids) {
        return ids == null ? new LinkedHashSet<>() : new LinkedHashSet<>(ids);
    }

    private Set<Long> getDomainIds(Long ontologyId) {
        return new LinkedHashSet<>(domainRelRepository.findByOntologyId(ontologyId).stream()
                .map(DomainOntologyRelEntity::getDomainId)
                .toList());
    }

    private void ensureCodeUnique(String code, Long currentId) {
        boolean exists = currentId == null
                ? ontologyRepository.existsByCodeIgnoreCase(code)
                : ontologyRepository.existsByCodeIgnoreCaseAndIdNot(code, currentId);
        if (exists) {
            throw new BusinessException(PlatformErrorCode.ONTOLOGY_CODE_EXISTS);
        }
    }

    private void ensureVersion(OntologyEntity entity, Long version) {
        if (!Objects.equals(entity.getVersion(), version)) {
            throw new BusinessException(PlatformErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
    }

    private OntologyResponse toResponse(OntologyEntity entity) {
        List<Long> domainIds = domainRelRepository.findByOntologyId(entity.getId()).stream()
                .map(DomainOntologyRelEntity::getDomainId)
                .toList();
        return new OntologyResponse(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.getDescription(),
                entity.getStatus(),
                domainIds,
                propertyRepository.countByOntologyId(entity.getId()),
                relationRepository.countBySourceOntologyIdOrTargetOntologyId(entity.getId(), entity.getId()),
                ruleRepository.countByOntologyIdAndDeletedFlagFalse(entity.getId()),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }
}
