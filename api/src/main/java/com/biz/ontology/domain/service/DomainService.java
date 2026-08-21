package com.biz.ontology.domain.service;

import com.biz.ontology.api.domain.dto.CreateDomainRequest;
import com.biz.ontology.api.domain.dto.CreateParentDomainRequest;
import com.biz.ontology.api.domain.dto.DomainResponse;
import com.biz.ontology.api.domain.dto.DomainTreeNodeResponse;
import com.biz.ontology.api.domain.dto.UpdateDomainRequest;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.domain.enums.DomainStatus;
import com.biz.ontology.domain.model.DomainEntity;
import com.biz.ontology.domain.repository.DomainRepository;
import com.biz.ontology.ontology.repository.DomainOntologyRelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class DomainService {

    private final DomainRepository domainRepository;
    private final DomainOntologyRelRepository domainOntologyRelRepository;

    public DomainService(
            DomainRepository domainRepository,
            DomainOntologyRelRepository domainOntologyRelRepository) {
        this.domainRepository = domainRepository;
        this.domainOntologyRelRepository = domainOntologyRelRepository;
    }

    @Transactional
    public DomainResponse create(CreateDomainRequest request) {
        String code = normalizeCode(request.getCode());
        ensureCodeUnique(code, null);
        validateParent(request.getParentId(), null);

        DomainEntity entity = new DomainEntity();
        entity.setParentId(request.getParentId());
        entity.setName(request.getName().trim());
        entity.setCode(code);
        entity.setDescription(normalizeDescription(request.getDescription()));
        entity.setStatus(Objects.requireNonNullElse(request.getStatus(), DomainStatus.ENABLED));
        entity.setSortOrder(Objects.requireNonNullElse(request.getSortOrder(), 0));
        return toResponse(domainRepository.saveAndFlush(entity));
    }

    @Transactional
    public DomainResponse createParent(CreateParentDomainRequest request) {
        String code = normalizeCode(request.getCode());
        ensureCodeUnique(code, null);

        List<Long> childIds = request.getChildDomainIds().stream().distinct().toList();
        List<DomainEntity> children = childIds.stream().map(this::findEntity).toList();
        if (children.stream().anyMatch(child ->
                child.getParentId() != null || domainRepository.existsByParentId(child.getId()))) {
            throw new BusinessException(
                    PlatformErrorCode.DOMAIN_PARENT_INVALID,
                    "只能选择尚未归属父领域且没有子领域的独立领域"
            );
        }

        DomainEntity parent = new DomainEntity();
        parent.setParentId(null);
        parent.setName(request.getName().trim());
        parent.setCode(code);
        parent.setDescription(normalizeDescription(request.getDescription()));
        parent.setStatus(Objects.requireNonNullElse(request.getStatus(), DomainStatus.ENABLED));
        parent.setSortOrder(Objects.requireNonNullElse(request.getSortOrder(), 0));
        parent = domainRepository.saveAndFlush(parent);

        for (DomainEntity child : children) {
            child.setParentId(parent.getId());
        }
        domainRepository.saveAllAndFlush(children);
        return toResponse(parent);
    }

    @Transactional(readOnly = true)
    public List<DomainTreeNodeResponse> tree() {
        List<DomainEntity> entities = domainRepository.findAllByOrderBySortOrderAscIdAsc();
        Map<Long, MutableTreeNode> nodes = new LinkedHashMap<>();
        for (DomainEntity entity : entities) {
            nodes.put(entity.getId(), new MutableTreeNode(entity));
        }

        List<MutableTreeNode> roots = new ArrayList<>();
        for (MutableTreeNode node : nodes.values()) {
            Long parentId = node.entity.getParentId();
            MutableTreeNode parent = parentId == null ? null : nodes.get(parentId);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.children.add(node);
            }
        }
        return roots.stream().map(MutableTreeNode::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<DomainResponse> page(String keyword, DomainStatus status, Pageable pageable) {
        Specification<DomainEntity> specification = (root, query, builder) -> builder.conjunction();
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
        return domainRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DomainResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public DomainResponse update(Long id, UpdateDomainRequest request) {
        DomainEntity entity = findEntity(id);
        ensureVersion(entity.getVersion(), request.getVersion());
        ensureCodeUnique(normalizeCode(request.getCode()), id);
        validateParent(request.getParentId(), id);

        entity.setParentId(request.getParentId());
        entity.setName(request.getName().trim());
        entity.setCode(normalizeCode(request.getCode()));
        entity.setDescription(normalizeDescription(request.getDescription()));
        entity.setStatus(request.getStatus());
        entity.setSortOrder(request.getSortOrder());
        return toResponse(domainRepository.saveAndFlush(entity));
    }

    @Transactional
    public DomainResponse updateStatus(Long id, DomainStatus status) {
        DomainEntity entity = findEntity(id);
        entity.setStatus(Objects.requireNonNull(status, "领域状态不能为空"));
        return toResponse(domainRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(Long id) {
        DomainEntity entity = findEntity(id);
        if (domainRepository.existsByParentId(id)) {
            throw new BusinessException(PlatformErrorCode.DOMAIN_HAS_CHILDREN);
        }
        if (domainOntologyRelRepository.existsByDomainId(id)) {
            throw new BusinessException(PlatformErrorCode.DOMAIN_HAS_ONTOLOGY);
        }
        domainRepository.delete(entity);
    }

    public DomainEntity findEntity(Long id) {
        return domainRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.DOMAIN_NOT_FOUND));
    }

    private void ensureCodeUnique(String code, Long currentId) {
        boolean exists = currentId == null
                ? domainRepository.existsByCodeIgnoreCase(code)
                : domainRepository.existsByCodeIgnoreCaseAndIdNot(code, currentId);
        if (exists) {
            throw new BusinessException(PlatformErrorCode.DOMAIN_CODE_EXISTS);
        }
    }

    private void validateParent(Long parentId, Long currentId) {
        if (parentId == null) {
            return;
        }
        if (Objects.equals(parentId, currentId)) {
            throw new BusinessException(PlatformErrorCode.DOMAIN_PARENT_INVALID, "领域不能以自身作为父节点");
        }
        DomainEntity parent = findEntity(parentId);
        Long ancestorId = parent.getParentId();
        while (ancestorId != null) {
            if (Objects.equals(ancestorId, currentId)) {
                throw new BusinessException(PlatformErrorCode.DOMAIN_PARENT_INVALID, "不能将领域移动到自己的子树中");
            }
            ancestorId = findEntity(ancestorId).getParentId();
        }
    }

    private void ensureVersion(Long actual, Long requested) {
        if (!Objects.equals(actual, requested)) {
            throw new BusinessException(PlatformErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {
        return StringUtils.hasText(description) ? description.trim() : null;
    }

    private DomainResponse toResponse(DomainEntity entity) {
        return new DomainResponse(
                entity.getId(),
                entity.getParentId(),
                entity.getName(),
                entity.getCode(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getSortOrder(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static final class MutableTreeNode {
        private final DomainEntity entity;
        private final List<MutableTreeNode> children = new ArrayList<>();

        private MutableTreeNode(DomainEntity entity) {
            this.entity = entity;
        }

        private DomainTreeNodeResponse toResponse() {
            return new DomainTreeNodeResponse(
                    entity.getId(),
                    entity.getParentId(),
                    entity.getName(),
                    entity.getCode(),
                    entity.getDescription(),
                    entity.getStatus(),
                    entity.getSortOrder(),
                    entity.getVersion(),
                    children.stream().map(MutableTreeNode::toResponse).toList()
            );
        }
    }
}
