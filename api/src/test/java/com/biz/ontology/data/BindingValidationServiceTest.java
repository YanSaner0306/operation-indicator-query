/**
 * 模块10测试：Binding 字段映射、唯一属性和筛选操作符校验。
 * 功能：验证合法映射可形成规范化结果，字符串字段拒绝大小比较，且目标本体唯一属性必须被映射。
 * 技术栈：JUnit 5、Mockito、AssertJ 与跨模块查询契约桩对象。
 */
package com.biz.ontology.data;

import com.biz.ontology.api.data.dto.*;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.data.binding.model.BindingFilterOperator;
import com.biz.ontology.data.binding.repository.*;
import com.biz.ontology.data.binding.validation.BindingValidationService;
import com.biz.ontology.data.model.DataSourceConfigEntity;
import com.biz.ontology.data.service.*;
import com.biz.ontology.ontology.enums.PropertyDataType;
import com.biz.ontology.ontology.query.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Types;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BindingValidationServiceTest {
    @Mock DataSourceConfigService dataSources; @Mock DataSourceMetadataService metadata; @Mock OntologySchemaQueryService schemas;
    @Mock OntologyTableBindingRepository bindings; @Mock OntologyFieldBindingRepository fields;
    BindingValidationService service; PropertyDefinition unique=new PropertyDefinition(11L,"订单号","orderId",PropertyDataType.STRING,true,true); PropertyDefinition amount=new PropertyDefinition(12L,"金额","amount",PropertyDataType.DECIMAL,false,false);
    @BeforeEach void setUp(){service=new BindingValidationService(dataSources,metadata,schemas,bindings,fields);DataSourceConfigEntity source=mock(DataSourceConfigEntity.class);when(source.getStatus()).thenReturn(ConfigStatus.ENABLED);when(source.getId()).thenReturn(7L);when(dataSources.requireEntity(7L)).thenReturn(source);when(metadata.columns(7L,"orders")).thenReturn(List.of(new ColumnMetadataResponse("order_id","VARCHAR",Types.VARCHAR,false,true),new ColumnMetadataResponse("amount","DECIMAL",Types.DECIMAL,false,false)));when(schemas.getOntologySchema(3L)).thenReturn(new OntologySchema(3L,"订单","Order",List.of(unique,amount),unique));when(schemas.listBindableProperties(3L)).thenReturn(List.of(unique,amount));when(schemas.validatePropertyMapping(anyLong(),anyString())).thenReturn(new MappingValidationResult(true,"ok"));}
    @Test void shouldAcceptUniqueMappingAndNumericFilter(){var result=service.validate(request(List.of(new SaveBindingRequest.FieldMappingItem("order_id",11L),new SaveBindingRequest.FieldMappingItem("amount",12L)),List.of(new SaveBindingRequest.FilterItem("amount",BindingFilterOperator.GE,"100.50"))),null,false);assertThat(result.mappings()).hasSize(2);assertThat(result.mappings().get(0).uniqueKey()).isTrue();assertThat(result.filters()).extracting(BindingValidationService.ValidatedFilter::sourceColumn).containsExactly("amount");}
    @Test void shouldRejectOrderingOperatorForStringColumn(){assertThatThrownBy(()->service.validate(request(List.of(new SaveBindingRequest.FieldMappingItem("order_id",11L)),List.of(new SaveBindingRequest.FilterItem("order_id",BindingFilterOperator.GT,"A"))),null,false)).isInstanceOf(BusinessException.class).hasMessageContaining("大小比较只支持");}
    @Test void shouldRequireUniquePropertyMapping(){assertThatThrownBy(()->service.validate(request(List.of(new SaveBindingRequest.FieldMappingItem("amount",12L)),List.of()),null,false)).isInstanceOf(BusinessException.class).hasMessageContaining("unique属性");}
    private SaveBindingRequest request(List<SaveBindingRequest.FieldMappingItem> mappings,List<SaveBindingRequest.FilterItem> filters){return new SaveBindingRequest("订单绑定",7L,null,"orders",3L,mappings,filters,null);}
}
