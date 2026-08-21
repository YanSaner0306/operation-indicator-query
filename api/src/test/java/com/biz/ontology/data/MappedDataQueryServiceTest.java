/**
 * 模块13测试：按本体属性和业务唯一键即时读取外部数据。
 * 功能：验证唯一启用 Binding 定位、唯一键列传递、目标属性取值及多候选歧义拒绝。
 * 技术栈：JUnit 5、Mockito、AssertJ 与 Spring ReflectionTestUtils。
 */
package com.biz.ontology.data;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.data.binding.model.*;
import com.biz.ontology.data.binding.query.BindingQueryExecutor;
import com.biz.ontology.data.binding.repository.*;
import com.biz.ontology.data.binding.service.BindingService;
import com.biz.ontology.data.model.DataSourceConfigEntity;
import com.biz.ontology.data.query.MappedDataQueryServiceImpl;
import com.biz.ontology.data.service.DataSourceConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MappedDataQueryServiceTest {
    @Test void shouldReadMappedPropertyByUniqueBusinessKey() throws Exception {OntologyTableBindingRepository bindings=mock(OntologyTableBindingRepository.class);OntologyFieldBindingRepository fields=mock(OntologyFieldBindingRepository.class);BindingService bindingService=mock(BindingService.class);DataSourceConfigService sources=mock(DataSourceConfigService.class);BindingQueryExecutor executor=mock(BindingQueryExecutor.class);var service=new MappedDataQueryServiceImpl(bindings,fields,bindingService,sources,executor);OntologyTableBindingEntity binding=new OntologyTableBindingEntity();ReflectionTestUtils.setField(binding,"id",5L);binding.setOntologyId(3L);binding.setDataSourceId(7L);binding.setStatus(ConfigStatus.ENABLED);OntologyFieldBindingEntity unique=mapping(5L,"order_id",11L,true),target=mapping(5L,"amount",12L,false);when(bindings.findByOntologyIdAndStatusAndDeletedFlagFalse(3L,ConfigStatus.ENABLED)).thenReturn(List.of(binding));when(fields.findCandidates(List.of(5L),12L)).thenReturn(List.of(target));when(bindingService.mappings(5L)).thenReturn(List.of(unique,target));when(bindingService.conditions(5L)).thenReturn(List.of());DataSourceConfigEntity source=new DataSourceConfigEntity();when(sources.requireEntity(7L)).thenReturn(source);when(executor.queryOne(source,binding,List.of(unique,target),List.of(),"order_id","SO-1")).thenReturn(Optional.of(Map.of("order_id","SO-1","amount",88.5)));var value=service.getPropertyValue(3L,12L,"SO-1").orElseThrow();assertThat(value.value()).isEqualTo(88.5);assertThat(value.bindingId()).isEqualTo(5L);}
    @Test void shouldRejectAmbiguousEnabledBindings(){OntologyTableBindingRepository bindings=mock(OntologyTableBindingRepository.class);OntologyFieldBindingRepository fields=mock(OntologyFieldBindingRepository.class);var service=new MappedDataQueryServiceImpl(bindings,fields,mock(BindingService.class),mock(DataSourceConfigService.class),mock(BindingQueryExecutor.class));OntologyTableBindingEntity a=new OntologyTableBindingEntity(),b=new OntologyTableBindingEntity();ReflectionTestUtils.setField(a,"id",1L);ReflectionTestUtils.setField(b,"id",2L);when(bindings.findByOntologyIdAndStatusAndDeletedFlagFalse(3L,ConfigStatus.ENABLED)).thenReturn(List.of(a,b));when(fields.findCandidates(List.of(1L,2L),12L)).thenReturn(List.of(mapping(1L,"amount",12L,false),mapping(2L,"amount",12L,false)));assertThatThrownBy(()->service.getPropertyValue(3L,12L,"SO-1")).isInstanceOf(BusinessException.class).hasMessageContaining("存在多条可用Binding候选");}
    private OntologyFieldBindingEntity mapping(Long bindingId,String column,Long propertyId,boolean unique){var m=new OntologyFieldBindingEntity();m.setBindingId(bindingId);m.setSourceColumn(column);m.setOntologyPropertyId(propertyId);m.setUniqueKey(unique);return m;}
}
