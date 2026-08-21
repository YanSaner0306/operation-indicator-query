/**
 * 模块6：数据源配置仓储。
 * 功能：提供排除软删除记录的查询、分页和名称唯一性检查。
 * 技术栈：Spring Data JPA与Specification。
 */
package com.biz.ontology.data.repository;

import com.biz.ontology.data.model.DataSourceConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface DataSourceConfigRepository extends JpaRepository<DataSourceConfigEntity, Long>, JpaSpecificationExecutor<DataSourceConfigEntity> {
    Optional<DataSourceConfigEntity> findByIdAndDeletedFlagFalse(Long id);
    boolean existsByNameAndDeletedFlagFalse(String name);
    boolean existsByNameAndIdNotAndDeletedFlagFalse(String name, Long id);
}
