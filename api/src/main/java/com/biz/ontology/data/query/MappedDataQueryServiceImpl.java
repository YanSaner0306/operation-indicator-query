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
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.util.*;

@Service
public class MappedDataQueryServiceImpl implements MappedDataQueryService {
    private final OntologyTableBindingRepository bindings;private final OntologyFieldBindingRepository fields;private final BindingService service;private final DataSourceConfigService sources;private final BindingQueryExecutor executor;
    public MappedDataQueryServiceImpl(OntologyTableBindingRepository a,OntologyFieldBindingRepository b,BindingService c,DataSourceConfigService d,BindingQueryExecutor e){bindings=a;fields=b;service=c;sources=d;executor=e;}
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
}
