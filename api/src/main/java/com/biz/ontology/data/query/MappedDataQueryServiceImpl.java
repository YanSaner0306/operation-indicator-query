/**
 * 模块13：本体属性即时查询合同实现。
 * 功能：定位唯一启用Binding，以unique映射字段和固定筛选条件参数化查询真实数据并返回目标属性值。
 * 技术栈：Spring Service、Spring Data JPA候选定位与JDBC PreparedStatement查询执行器。
 */
package com.biz.ontology.data.query;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.*;
import com.biz.ontology.data.binding.model.*;
import com.biz.ontology.data.binding.query.BindingQueryExecutor;
import com.biz.ontology.data.binding.repository.*;
import com.biz.ontology.data.binding.service.BindingService;
import com.biz.ontology.data.model.DataSourceConfigEntity;
import com.biz.ontology.data.service.DataSourceConfigService;
import com.biz.ontology.ontology.model.OntologyPropertyEntity;
import com.biz.ontology.ontology.repository.OntologyPropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.util.*;

@Service
public class MappedDataQueryServiceImpl implements MappedDataQueryService {
    private final OntologyTableBindingRepository bindings;private final OntologyFieldBindingRepository fields;private final BindingService service;private final DataSourceConfigService sources;private final BindingQueryExecutor executor;private final OntologyPropertyRepository properties;
    @Autowired
    public MappedDataQueryServiceImpl(OntologyTableBindingRepository a,OntologyFieldBindingRepository b,BindingService c,DataSourceConfigService d,BindingQueryExecutor e,OntologyPropertyRepository f){bindings=a;fields=b;service=c;sources=d;executor=e;properties=f;}
    public MappedDataQueryServiceImpl(OntologyTableBindingRepository a,OntologyFieldBindingRepository b,BindingService c,DataSourceConfigService d,BindingQueryExecutor e){this(a,b,c,d,e,null);}
    @Override public Optional<MappedValue> getPropertyValue(Long ontologyId,Long propertyId,Object businessKey){
        List<OntologyTableBindingEntity> enabled=bindings.findByOntologyIdAndStatusAndDeletedFlagFalse(ontologyId,ConfigStatus.ENABLED);List<Long> ids=enabled.stream().map(OntologyTableBindingEntity::getId).toList();
        if(ids.isEmpty())throw new BusinessException(PlatformErrorCode.BINDING_NOT_FOUND);List<OntologyFieldBindingEntity> candidates=fields.findCandidates(ids,propertyId);
        if(candidates.isEmpty())throw new BusinessException(PlatformErrorCode.BINDING_NOT_FOUND);if(candidates.size()>1)throw new BusinessException(PlatformErrorCode.BINDING_AMBIGUOUS);
        OntologyFieldBindingEntity target=candidates.get(0);OntologyTableBindingEntity binding=enabled.stream().filter(v->v.getId().equals(target.getBindingId())).findFirst().orElseThrow();
        List<OntologyFieldBindingEntity> mappings=service.mappings(binding.getId());OntologyFieldBindingEntity unique=mappings.stream().filter(OntologyFieldBindingEntity::isUniqueKey).findFirst().orElseThrow(()->new BusinessException(PlatformErrorCode.BINDING_INVALID));
        DataSourceConfigEntity source=sources.requireEntity(binding.getDataSourceId());
        try{return executor.queryOne(source,binding,mappings,service.conditions(binding.getId()),unique.getSourceColumn(),businessKey).map(row->new MappedValue(binding.getId(),ontologyId,propertyId,businessKey,row.get(target.getSourceColumn())));}
        catch(SQLException exception){throw new BusinessException(PlatformErrorCode.BINDING_PREVIEW_FAILED);}
    }

    @Override public Optional<MappedRecord> getRecord(Long ontologyId,Object businessKey){
        BindingContext context=requireSingleBinding(ontologyId);
        try{return executor.queryOne(context.source(),context.binding(),context.mappings(),context.conditions(),context.unique().getSourceColumn(),businessKey).map(row->toRecord(context,row));}
        catch(SQLException exception){throw new BusinessException(PlatformErrorCode.BINDING_PREVIEW_FAILED);}
    }

    @Override public List<MappedRecord> findRecords(Long ontologyId,Long propertyId,Object value,int limit){
        List<OntologyTableBindingEntity> enabled=enabledBindings(ontologyId);List<Long> ids=enabled.stream().map(OntologyTableBindingEntity::getId).toList();
        List<OntologyFieldBindingEntity> candidates=fields.findCandidates(ids,propertyId);
        if(candidates.isEmpty())throw new BusinessException(PlatformErrorCode.BINDING_NOT_FOUND);
        if(candidates.size()>1)throw new BusinessException(PlatformErrorCode.BINDING_AMBIGUOUS);
        OntologyFieldBindingEntity filterMapping=candidates.get(0);OntologyTableBindingEntity binding=enabled.stream().filter(v->v.getId().equals(filterMapping.getBindingId())).findFirst().orElseThrow();
        BindingContext context=context(binding);
        try{return executor.queryMany(context.source(),binding,context.mappings(),context.conditions(),filterMapping.getSourceColumn(),value,limit).stream().map(row->toRecord(context,row)).toList();}
        catch(SQLException exception){throw new BusinessException(PlatformErrorCode.BINDING_PREVIEW_FAILED);}
    }

    private List<OntologyTableBindingEntity> enabledBindings(Long ontologyId){
        List<OntologyTableBindingEntity> enabled=bindings.findByOntologyIdAndStatusAndDeletedFlagFalse(ontologyId,ConfigStatus.ENABLED);
        if(enabled.isEmpty())throw new BusinessException(PlatformErrorCode.BINDING_NOT_FOUND);return enabled;
    }

    private BindingContext requireSingleBinding(Long ontologyId){
        List<OntologyTableBindingEntity> enabled=enabledBindings(ontologyId);if(enabled.size()>1)throw new BusinessException(PlatformErrorCode.BINDING_AMBIGUOUS);return context(enabled.get(0));
    }

    private BindingContext context(OntologyTableBindingEntity binding){
        List<OntologyFieldBindingEntity> mappings=service.mappings(binding.getId());
        OntologyFieldBindingEntity unique=mappings.stream().filter(OntologyFieldBindingEntity::isUniqueKey).findFirst().orElseThrow(()->new BusinessException(PlatformErrorCode.BINDING_INVALID));
        List<Long> propertyIds=mappings.stream().map(OntologyFieldBindingEntity::getOntologyPropertyId).toList();Map<Long,String> codes=new HashMap<>();
        for(OntologyPropertyEntity property:properties.findByOntologyIdOrderBySortOrderAscIdAsc(binding.getOntologyId()))if(propertyIds.contains(property.getId()))codes.put(property.getId(),property.getCode());
        if(codes.size()!=new HashSet<>(propertyIds).size())throw new BusinessException(PlatformErrorCode.BINDING_INVALID);
        return new BindingContext(binding,mappings,service.conditions(binding.getId()),unique,sources.requireEntity(binding.getDataSourceId()),Map.copyOf(codes));
    }

    private MappedRecord toRecord(BindingContext context,Map<String,Object> row){
        Map<String,Object> values=new LinkedHashMap<>();for(OntologyFieldBindingEntity mapping:context.mappings())values.put(context.propertyCodes().get(mapping.getOntologyPropertyId()),row.get(mapping.getSourceColumn()));
        return new MappedRecord(context.binding().getId(),context.binding().getOntologyId(),row.get(context.unique().getSourceColumn()),Collections.unmodifiableMap(values));
    }

    private record BindingContext(OntologyTableBindingEntity binding,List<OntologyFieldBindingEntity> mappings,List<BindingFilterConditionEntity> conditions,OntologyFieldBindingEntity unique,DataSourceConfigEntity source,Map<Long,String> propertyCodes){}
}
