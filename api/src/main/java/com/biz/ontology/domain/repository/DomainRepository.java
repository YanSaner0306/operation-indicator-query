package com.biz.ontology.domain.repository;

import com.biz.ontology.domain.model.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 业务领域数据访问接口。
 *
 * 负责对 biz_domain 表执行新增、查询、修改和删除操作。
 */
public interface DomainRepository
        extends JpaRepository<DomainEntity, Long>, JpaSpecificationExecutor<DomainEntity> {

    /**
     * 根据领域编码查询领域。
     *
     * @param code 领域编码
     * @return 查询到的领域；不存在时为空
     */
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByParentId(Long parentId);

    List<DomainEntity> findAllByOrderBySortOrderAscIdAsc();
}
