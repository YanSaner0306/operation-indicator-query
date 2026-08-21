/**
 * 模块9-13：Binding主表仓储。
 * 功能：提供软删除查询、名称唯一性、启用候选和数据源/本体引用检查。
 * 技术栈：Spring Data JPA、Specification与派生查询。
 */
package com.biz.ontology.data.binding.repository;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.data.binding.model.OntologyTableBindingEntity;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface OntologyTableBindingRepository extends JpaRepository<OntologyTableBindingEntity,Long>, JpaSpecificationExecutor<OntologyTableBindingEntity> {
    Optional<OntologyTableBindingEntity> findByIdAndDeletedFlagFalse(Long id);
    boolean existsByNameAndDeletedFlagFalse(String name);
    boolean existsByNameAndIdNotAndDeletedFlagFalse(String name,Long id);
    boolean existsByDataSourceIdAndStatusAndDeletedFlagFalse(Long dataSourceId,ConfigStatus status);
    boolean existsByOntologyIdAndDeletedFlagFalse(Long ontologyId);
    List<OntologyTableBindingEntity> findByOntologyIdAndStatusAndDeletedFlagFalse(Long ontologyId,ConfigStatus status);
}
