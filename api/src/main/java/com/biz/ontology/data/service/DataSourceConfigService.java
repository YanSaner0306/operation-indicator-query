/**
 * 模块6：数据源配置应用服务。
 * 功能：完成分页CRUD、AES-GCM密码保存、空密码保留、启停和软删除闭环。
 * 技术栈：Spring事务、Spring Data JPA Specification与乐观锁。
 */
package com.biz.ontology.data.service;

import com.biz.ontology.api.common.PageResponse;
import com.biz.ontology.api.data.dto.*;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.data.model.*;
import com.biz.ontology.data.repository.DataSourceConfigRepository;
import com.biz.ontology.data.security.PasswordEncryptionService;
import com.biz.ontology.data.connection.DynamicDataSourcePoolRegistry;
import com.biz.ontology.data.binding.repository.OntologyTableBindingRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class DataSourceConfigService {
    private final DataSourceConfigRepository repository;
    private final PasswordEncryptionService encryptionService;
    private final DynamicDataSourcePoolRegistry poolRegistry;
    private final OntologyTableBindingRepository bindingRepository;
    public DataSourceConfigService(DataSourceConfigRepository repository, PasswordEncryptionService encryptionService,
                                   DynamicDataSourcePoolRegistry poolRegistry,OntologyTableBindingRepository bindingRepository) {
        this.repository = repository; this.encryptionService = encryptionService; this.poolRegistry = poolRegistry;this.bindingRepository=bindingRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<DataSourceResponse> page(String keyword, ConfigStatus status, int page, int size) {
        Specification<DataSourceConfigEntity> specification = (root, query, cb) -> {
            List<Predicate> values = new ArrayList<>();
            values.add(cb.isFalse(root.get("deletedFlag")));
            if (status != null) values.add(cb.equal(root.get("status"), status));
            if (keyword != null && !keyword.isBlank()) values.add(cb.like(cb.lower(root.get("name")), "%" + keyword.trim().toLowerCase() + "%"));
            return cb.and(values.toArray(Predicate[]::new));
        };
        Page<DataSourceConfigEntity> result = repository.findAll(specification, PageRequest.of(page - 1, size, Sort.by("name")));
        return new PageResponse<>(result.stream().map(this::toResponse).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public DataSourceResponse get(Long id) { return toResponse(requireEntity(id)); }

    @Transactional
    public DataSourceResponse create(SaveDataSourceRequest request) {
        if (request.password() == null || request.password().isBlank()) throw new BusinessException(PlatformErrorCode.DATASOURCE_CONFIG_INVALID, "新建数据源必须填写密码");
        String name = request.name().trim();
        if (repository.existsByNameAndDeletedFlagFalse(name)) throw new BusinessException(PlatformErrorCode.DATASOURCE_NAME_EXISTS);
        DataSourceConfigEntity entity = new DataSourceConfigEntity();
        apply(entity, request, true);
        entity.setStatus(ConfigStatus.DISABLED);
        entity.setLastTestStatus(ConnectionTestStatus.UNTESTED);
        return toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public DataSourceResponse update(Long id, SaveDataSourceRequest request) {
        DataSourceConfigEntity entity = requireEntity(id);
        requireVersion(entity, request.version());
        String name = request.name().trim();
        if (repository.existsByNameAndIdNotAndDeletedFlagFalse(name, id)) throw new BusinessException(PlatformErrorCode.DATASOURCE_NAME_EXISTS);
        apply(entity, request, false);
        poolRegistry.evict(id);
        return toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public DataSourceResponse updateStatus(Long id, UpdateDataSourceStatusRequest request) {
        DataSourceConfigEntity entity = requireEntity(id);
        requireVersion(entity, request.version());
        entity.setStatus(request.status());
        if (request.status() == ConfigStatus.DISABLED) poolRegistry.evict(id);
        return toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(Long id, Long version) {
        DataSourceConfigEntity entity = requireEntity(id);
        requireVersion(entity, version);
        if(bindingRepository.existsByDataSourceIdAndStatusAndDeletedFlagFalse(id,ConfigStatus.ENABLED)) throw new BusinessException(PlatformErrorCode.DATASOURCE_REFERENCED);
        entity.setStatus(ConfigStatus.DISABLED);
        entity.setDeletedFlag(true);
        poolRegistry.evict(id);
        repository.saveAndFlush(entity);
    }

    public DataSourceConfigEntity requireEntity(Long id) {
        return repository.findByIdAndDeletedFlagFalse(id).orElseThrow(() -> new BusinessException(PlatformErrorCode.DATASOURCE_NOT_FOUND));
    }

    private void apply(DataSourceConfigEntity entity, SaveDataSourceRequest request, boolean passwordRequired) {
        entity.setName(request.name().trim()); entity.setDbType(request.dbType()); entity.setHost(request.host().trim());
        entity.setPort(request.port()); entity.setDatabaseName(request.databaseName().trim()); entity.setUsername(request.username().trim());
        if (passwordRequired || (request.password() != null && !request.password().isBlank())) {
            PasswordEncryptionService.EncryptedPassword encrypted = encryptionService.encrypt(request.password());
            entity.setPasswordCipher(encrypted.ciphertext()); entity.setPasswordIv(encrypted.iv()); entity.setKeyVersion(encrypted.keyVersion());
        }
        entity.setLastTestStatus(ConnectionTestStatus.UNTESTED); entity.setLastTestAt(null);
    }

    private void requireVersion(DataSourceConfigEntity entity, Long version) {
        if (version == null || !Objects.equals(entity.getVersion(), version)) throw new BusinessException(PlatformErrorCode.OPTIMISTIC_LOCK_CONFLICT);
    }

    public DataSourceResponse toResponse(DataSourceConfigEntity e) {
        return new DataSourceResponse(e.getId(), e.getName(), e.getDbType(), e.getHost(), e.getPort(), e.getDatabaseName(), e.getUsername(), e.getStatus(), e.getLastTestStatus(), e.getLastTestAt(), e.getVersion(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
