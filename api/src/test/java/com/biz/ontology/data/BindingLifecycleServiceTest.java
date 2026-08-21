/**
 * 模块9与12测试：Binding 生命周期保护规则。
 * 功能：验证启用中的绑定不能编辑或删除，防止运行中的规则读取配置被原地改变。
 * 技术栈：JUnit 5、Mockito、AssertJ 与应用服务单元测试。
 */
package com.biz.ontology.data;

import com.biz.ontology.api.data.dto.SaveBindingRequest;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.data.binding.model.OntologyTableBindingEntity;
import com.biz.ontology.data.binding.repository.*;
import com.biz.ontology.data.binding.service.BindingService;
import com.biz.ontology.data.binding.validation.BindingValidationService;
import com.biz.ontology.data.service.DataSourceConfigService;
import com.biz.ontology.ontology.query.OntologySchemaQueryService;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BindingLifecycleServiceTest {
    @Test void enabledBindingShouldRejectUpdateAndDelete(){OntologyTableBindingRepository bindings=mock(OntologyTableBindingRepository.class);var entity=new OntologyTableBindingEntity();entity.setStatus(ConfigStatus.ENABLED);when(bindings.findByIdAndDeletedFlagFalse(9L)).thenReturn(Optional.of(entity));BindingService service=new BindingService(bindings,mock(OntologyFieldBindingRepository.class),mock(BindingFilterConditionRepository.class),mock(BindingValidationService.class),mock(DataSourceConfigService.class),mock(OntologySchemaQueryService.class));SaveBindingRequest request=new SaveBindingRequest("绑定",1L,null,"t",1L,List.of(new SaveBindingRequest.FieldMappingItem("id",1L)),List.of(),0L);assertThatThrownBy(()->service.update(9L,request)).isInstanceOf(BusinessException.class).hasMessageContaining("已启用Binding不能修改或删除");assertThatThrownBy(()->service.delete(9L,0L)).isInstanceOf(BusinessException.class).hasMessageContaining("已启用Binding不能修改或删除");verify(bindings,never()).saveAndFlush(any());}
}
