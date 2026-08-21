/**
 * 模块10：Binding全量业务校验服务。
 * 功能：校验数据源/表/本体状态、字段白名单、类型兼容、unique映射、筛选值和启用重叠。
 * 技术栈：跨模块OntologySchemaQueryService合同、JDBC元数据DTO与纯Java集合校验。
 */
package com.biz.ontology.data.binding.validation;

import com.biz.ontology.api.data.dto.*;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.*;
import com.biz.ontology.data.binding.model.BindingFilterOperator;
import com.biz.ontology.data.binding.repository.*;
import com.biz.ontology.data.model.DataSourceConfigEntity;
import com.biz.ontology.data.service.*;
import com.biz.ontology.ontology.query.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BindingValidationService {
    private final DataSourceConfigService dataSources; private final DataSourceMetadataService metadata;
    private final OntologySchemaQueryService schemas; private final OntologyTableBindingRepository bindings;
    private final OntologyFieldBindingRepository fields;
    public BindingValidationService(DataSourceConfigService a,DataSourceMetadataService b,OntologySchemaQueryService c,
                                    OntologyTableBindingRepository d,OntologyFieldBindingRepository e){dataSources=a;metadata=b;schemas=c;bindings=d;fields=e;}

    public ValidatedBinding validate(SaveBindingRequest request,Long currentBindingId,boolean checkOverlap) {
        DataSourceConfigEntity source=dataSources.requireEntity(request.dataSourceId());
        if(source.getStatus()!=ConfigStatus.ENABLED) throw invalid("数据源必须处于启用状态");
        List<ColumnMetadataResponse> columnList=metadata.columns(source.getId(),request.tableName());
        Map<String,ColumnMetadataResponse> columns=new LinkedHashMap<>(); columnList.forEach(c->columns.put(c.name(),c));
        OntologySchema schema=schemas.getOntologySchema(request.ontologyId());
        List<PropertyDefinition> bindable=schemas.listBindableProperties(request.ontologyId());
        Map<Long,PropertyDefinition> properties=new HashMap<>(); bindable.forEach(p->properties.put(p.id(),p));
        Set<String> usedColumns=new HashSet<>(); Set<Long> usedProperties=new HashSet<>(); List<ValidatedMapping> mappings=new ArrayList<>();
        int sequence=0;
        for(SaveBindingRequest.FieldMappingItem item:request.mappings()) {
            ColumnMetadataResponse column=columns.get(item.sourceColumn()); PropertyDefinition property=properties.get(item.ontologyPropertyId());
            if(column==null) throw invalid("映射字段不存在："+item.sourceColumn());
            if(property==null) throw invalid("本体属性不存在、已禁用或不属于目标本体："+item.ontologyPropertyId());
            if(!usedColumns.add(column.name())||!usedProperties.add(property.id())) throw invalid("字段和本体属性不能重复映射");
            MappingValidationResult compatibility=schemas.validatePropertyMapping(property.id(),column.typeName());
            if(!compatibility.valid()) throw invalid(column.name()+"与"+property.code()+"类型不兼容");
            mappings.add(new ValidatedMapping(column.name(),column.typeName(),property,property.uniqueFlag(),sequence++));
        }
        if(schema.uniqueProperty()==null) throw invalid("目标本体没有unique属性");
        long uniqueCount=mappings.stream().filter(m->m.property().id().equals(schema.uniqueProperty().id())).count();
        if(uniqueCount!=1) throw invalid("必须且只能映射目标本体的unique属性");

        List<ValidatedFilter> filters=new ArrayList<>(); sequence=0;
        for(SaveBindingRequest.FilterItem item:request.filters()) {
            ColumnMetadataResponse column=columns.get(item.sourceColumn()); if(column==null) throw invalid("筛选字段不存在："+item.sourceColumn());
            validateOperator(item.operator(),item.value(),column.typeName());
            filters.add(new ValidatedFilter(column.name(),column.typeName(),item.operator(),item.value(),sequence++));
        }
        if(checkOverlap) validateOverlap(request.ontologyId(),mappings,currentBindingId);
        return new ValidatedBinding(source,schema,List.copyOf(mappings),List.copyOf(filters));
    }

    public void validateOverlap(Long ontologyId,List<ValidatedMapping> mappings,Long currentBindingId) {
        Set<Long> propertyIds=mappings.stream().map(m->m.property().id()).collect(java.util.stream.Collectors.toSet());
        List<Long> enabledIds=bindings.findByOntologyIdAndStatusAndDeletedFlagFalse(ontologyId,ConfigStatus.ENABLED).stream()
                .map(v->v.getId()).filter(id->!Objects.equals(id,currentBindingId)).toList();
        if(enabledIds.isEmpty()) return;
        for(Long propertyId:propertyIds) if(!fields.findCandidates(enabledIds,propertyId).isEmpty()) throw new BusinessException(PlatformErrorCode.BINDING_AMBIGUOUS);
    }

    private void validateOperator(BindingFilterOperator operator,String value,String type) {
        if((operator==BindingFilterOperator.IS_NULL||operator==BindingFilterOperator.NOT_NULL)) {
            if(value!=null&&!value.isBlank()) throw invalid("空值操作符不能填写比较值"); return;
        }
        if(value==null||value.isBlank()) throw invalid("筛选条件必须填写比较值");
        if(EnumSet.of(BindingFilterOperator.GT,BindingFilterOperator.GE,BindingFilterOperator.LT,BindingFilterOperator.LE).contains(operator)&&!FilterValueCodec.numericOrTemporal(type)) throw invalid("大小比较只支持数字或日期时间字段");
        if(operator==BindingFilterOperator.IN) FilterValueCodec.parseMany(value,type); else FilterValueCodec.parseOne(value,type);
    }
    private BusinessException invalid(String message){return new BusinessException(PlatformErrorCode.BINDING_INVALID,message);}
    public record ValidatedBinding(DataSourceConfigEntity dataSource,OntologySchema ontology,List<ValidatedMapping> mappings,List<ValidatedFilter> filters){}
    public record ValidatedMapping(String sourceColumn,String sourceDataType,PropertyDefinition property,boolean uniqueKey,int sequence){}
    public record ValidatedFilter(String sourceColumn,String sourceDataType,BindingFilterOperator operator,String value,int sequence){}
}
