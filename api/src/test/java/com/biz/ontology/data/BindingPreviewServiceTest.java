/**
 * 模块11测试：Binding预览敏感字段保护。
 * 功能：验证源字段、本体属性和业务唯一键中的身份证等敏感值不会返回明文。
 * 技术栈：JUnit 5、Mockito与AssertJ。
 */
package com.biz.ontology.data;

import com.biz.ontology.api.data.dto.BindingPreviewResponse;
import com.biz.ontology.data.binding.model.OntologyFieldBindingEntity;
import com.biz.ontology.data.binding.model.OntologyTableBindingEntity;
import com.biz.ontology.data.binding.query.BindingQueryExecutor;
import com.biz.ontology.data.binding.repository.BindingTestLogRepository;
import com.biz.ontology.data.binding.repository.OntologyTableBindingRepository;
import com.biz.ontology.data.binding.service.BindingPreviewService;
import com.biz.ontology.data.binding.service.BindingService;
import com.biz.ontology.data.model.DataSourceConfigEntity;
import com.biz.ontology.data.service.DataSourceConfigService;
import com.biz.ontology.ontology.enums.PropertyDataType;
import com.biz.ontology.ontology.query.OntologySchema;
import com.biz.ontology.ontology.query.OntologySchemaQueryService;
import com.biz.ontology.ontology.query.PropertyDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BindingPreviewServiceTest {
    @Test
    void shouldMaskSensitiveSourcePropertyAndExternalKey() throws Exception {
        BindingService bindingService = mock(BindingService.class);
        DataSourceConfigService dataSources = mock(DataSourceConfigService.class);
        BindingQueryExecutor executor = mock(BindingQueryExecutor.class);
        OntologySchemaQueryService schemas = mock(OntologySchemaQueryService.class);
        OntologyTableBindingRepository bindings = mock(OntologyTableBindingRepository.class);
        BindingTestLogRepository logs = mock(BindingTestLogRepository.class);
        BindingPreviewService service = new BindingPreviewService(bindingService, dataSources, executor, schemas, bindings, logs);

        OntologyTableBindingEntity binding = new OntologyTableBindingEntity();
        ReflectionTestUtils.setField(binding, "id", 1L);
        binding.setDataSourceId(2L);
        binding.setOntologyId(3L);
        DataSourceConfigEntity source = new DataSourceConfigEntity();

        PropertyDefinition idCard = new PropertyDefinition(11L, "身份证号", "IDCARD", PropertyDataType.STRING, true, true);
        PropertyDefinition amount = new PropertyDefinition(12L, "金额", "AMOUNT", PropertyDataType.DECIMAL, false, false);
        OntologyFieldBindingEntity idCardMapping = mapping("ID_CARD", 11L, true, 0);
        OntologyFieldBindingEntity amountMapping = mapping("AMOUNT", 12L, false, 1);
        List<OntologyFieldBindingEntity> mappings = List.of(idCardMapping, amountMapping);

        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("ID_CARD", "110101199001011234");
        raw.put("AMOUNT", 1288.5);

        when(bindingService.require(1L)).thenReturn(binding);
        when(bindingService.mappings(1L)).thenReturn(mappings);
        when(bindingService.conditions(1L)).thenReturn(List.of());
        when(dataSources.requireEntity(2L)).thenReturn(source);
        when(executor.queryOne(source, binding, mappings, List.of(), null, null)).thenReturn(Optional.of(raw));
        when(schemas.getOntologySchema(3L)).thenReturn(new OntologySchema(3L, "订单", "ORDER", List.of(idCard, amount), idCard));

        BindingPreviewResponse response = service.preview(1L);

        assertThat(response.externalKey()).isEqualTo("11****34");
        assertThat(response.sourceValues()).containsEntry("ID_CARD", "11****34").containsEntry("AMOUNT", 1288.5);
        assertThat(response.properties()).containsEntry("IDCARD", "11****34").containsEntry("AMOUNT", 1288.5);
        verify(bindings).save(binding);
        verify(logs).save(any());
    }

    private OntologyFieldBindingEntity mapping(String column, Long propertyId, boolean unique, int sequence) {
        OntologyFieldBindingEntity mapping = new OntologyFieldBindingEntity();
        mapping.setSourceColumn(column);
        mapping.setSourceDataType("VARCHAR");
        mapping.setOntologyPropertyId(propertyId);
        mapping.setUniqueKey(unique);
        mapping.setSequenceNo(sequence);
        return mapping;
    }
}
