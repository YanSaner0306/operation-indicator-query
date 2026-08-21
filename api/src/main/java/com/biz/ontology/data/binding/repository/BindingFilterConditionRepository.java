/**
 * 模块9-13：Binding筛选条件仓储。
 * 功能：按配置顺序加载结构化条件，并在更新时批量替换旧条件。
 * 技术栈：Spring Data JPA与JPQL批量删除。
 */
package com.biz.ontology.data.binding.repository;

import com.biz.ontology.data.binding.model.BindingFilterConditionEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BindingFilterConditionRepository extends JpaRepository<BindingFilterConditionEntity,Long> {
    List<BindingFilterConditionEntity> findByBindingIdOrderBySequenceNoAsc(Long bindingId);
    @Modifying @Query("delete from BindingFilterConditionEntity f where f.bindingId=:bindingId")
    void deleteByBindingId(@Param("bindingId") Long bindingId);
}
