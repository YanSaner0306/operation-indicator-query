/**
 * 模块9-13：字段映射仓储。
 * 功能：按Binding顺序读取映射、原子替换子项并按本体属性定位启用候选。
 * 技术栈：Spring Data JPA与批量删除查询。
 */
package com.biz.ontology.data.binding.repository;

import com.biz.ontology.data.binding.model.OntologyFieldBindingEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface OntologyFieldBindingRepository extends JpaRepository<OntologyFieldBindingEntity,Long> {
    List<OntologyFieldBindingEntity> findByBindingIdOrderBySequenceNoAsc(Long bindingId);
    boolean existsByOntologyPropertyId(Long propertyId);
    @Query("select count(f)>0 from OntologyFieldBindingEntity f, OntologyTableBindingEntity b where f.bindingId=b.id and f.ontologyPropertyId=:propertyId and b.deletedFlag=false")
    boolean existsActiveProperty(@Param("propertyId") Long propertyId);
    @Modifying @Query("delete from OntologyFieldBindingEntity f where f.bindingId=:bindingId")
    void deleteByBindingId(@Param("bindingId") Long bindingId);
    @Query("select f from OntologyFieldBindingEntity f where f.bindingId in :bindingIds and f.ontologyPropertyId=:propertyId")
    List<OntologyFieldBindingEntity> findCandidates(@Param("bindingIds") Collection<Long> bindingIds,@Param("propertyId") Long propertyId);
}
