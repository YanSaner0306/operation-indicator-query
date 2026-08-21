/**
 * 模块9-12：Binding配置应用服务。
 * 功能：在单一事务中完成主表、字段映射、筛选条件CRUD，并实施禁用编辑、乐观锁和软删除规则。
 * 技术栈：Spring事务、Spring Data JPA Specification和跨模块查询Service合同。
 */
package com.biz.ontology.data.binding.service;

import com.biz.ontology.api.common.PageResponse;
import com.biz.ontology.api.data.dto.*;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.*;
import com.biz.ontology.data.binding.model.*;
import com.biz.ontology.data.binding.repository.*;
import com.biz.ontology.data.binding.validation.BindingValidationService;
import com.biz.ontology.data.model.ConnectionTestStatus;
import com.biz.ontology.data.service.DataSourceConfigService;
import com.biz.ontology.ontology.query.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class BindingService {
    private final OntologyTableBindingRepository bindings; private final OntologyFieldBindingRepository fields;
    private final BindingFilterConditionRepository filters; private final BindingValidationService validation;
    private final DataSourceConfigService dataSources; private final OntologySchemaQueryService schemas;
    public BindingService(OntologyTableBindingRepository a,OntologyFieldBindingRepository b,BindingFilterConditionRepository c,
                          BindingValidationService d,DataSourceConfigService e,OntologySchemaQueryService f){bindings=a;fields=b;filters=c;validation=d;dataSources=e;schemas=f;}

    @Transactional(readOnly=true)
    public PageResponse<BindingResponse> page(String keyword,Long ontologyId,Long dataSourceId,ConfigStatus status,int page,int size){
        Specification<OntologyTableBindingEntity> spec=(root,query,cb)->{List<Predicate> p=new ArrayList<>();p.add(cb.isFalse(root.get("deletedFlag")));
            if(keyword!=null&&!keyword.isBlank())p.add(cb.like(cb.lower(root.get("name")),"%"+keyword.trim().toLowerCase()+"%"));
            if(ontologyId!=null)p.add(cb.equal(root.get("ontologyId"),ontologyId)); if(dataSourceId!=null)p.add(cb.equal(root.get("dataSourceId"),dataSourceId)); if(status!=null)p.add(cb.equal(root.get("status"),status)); return cb.and(p.toArray(Predicate[]::new));};
        Page<OntologyTableBindingEntity> result=bindings.findAll(spec,PageRequest.of(page-1,size,Sort.by("updatedAt").descending()));
        return new PageResponse<>(result.stream().map(this::toResponse).toList(),page,size,result.getTotalElements());
    }
    @Transactional(readOnly=true) public BindingResponse get(Long id){return toResponse(require(id));}

    @Transactional public BindingResponse create(SaveBindingRequest request){
        String name=request.name().trim(); if(bindings.existsByNameAndDeletedFlagFalse(name))throw new BusinessException(PlatformErrorCode.BINDING_NAME_EXISTS);
        BindingValidationService.ValidatedBinding valid=validation.validate(request,null,false);
        OntologyTableBindingEntity entity=new OntologyTableBindingEntity(); applyMain(entity,request); entity.setStatus(ConfigStatus.DISABLED); entity.setLastTestStatus(ConnectionTestStatus.UNTESTED);
        entity=bindings.saveAndFlush(entity); saveChildren(entity.getId(),valid); return toResponse(entity);
    }
    @Transactional public BindingResponse update(Long id,SaveBindingRequest request){
        OntologyTableBindingEntity entity=require(id); requireDisabled(entity); requireVersion(entity.getVersion(),request.version());
        if(bindings.existsByNameAndIdNotAndDeletedFlagFalse(request.name().trim(),id))throw new BusinessException(PlatformErrorCode.BINDING_NAME_EXISTS);
        BindingValidationService.ValidatedBinding valid=validation.validate(request,id,false); applyMain(entity,request); entity.setLastTestStatus(ConnectionTestStatus.UNTESTED);entity.setLastTestAt(null);
        fields.deleteByBindingId(id);filters.deleteByBindingId(id);fields.flush();filters.flush(); saveChildren(id,valid); return toResponse(bindings.saveAndFlush(entity));
    }
    @Transactional(readOnly=true) public BindingValidationResponse validate(Long id){OntologyTableBindingEntity entity=require(id); validation.validate(toRequest(entity),id,false);return new BindingValidationResponse(true,List.of("基础配置、字段映射、unique属性和筛选条件校验通过"));}
    @Transactional public BindingResponse updateStatus(Long id,UpdateBindingStatusRequest request){
        OntologyTableBindingEntity entity=require(id);requireVersion(entity.getVersion(),request.version());
        if(request.status()==ConfigStatus.ENABLED){if(entity.getLastTestStatus()!=ConnectionTestStatus.SUCCESS)throw new BusinessException(PlatformErrorCode.BINDING_TEST_REQUIRED); BindingValidationService.ValidatedBinding valid=validation.validate(toRequest(entity),id,true);validation.validateOverlap(entity.getOntologyId(),valid.mappings(),id);}
        entity.setStatus(request.status());return toResponse(bindings.saveAndFlush(entity));
    }
    @Transactional public void delete(Long id,Long version){OntologyTableBindingEntity entity=require(id);requireDisabled(entity);requireVersion(entity.getVersion(),version);entity.setDeletedFlag(true);bindings.saveAndFlush(entity);}
    public OntologyTableBindingEntity require(Long id){return bindings.findByIdAndDeletedFlagFalse(id).orElseThrow(()->new BusinessException(PlatformErrorCode.BINDING_NOT_FOUND));}
    public List<OntologyFieldBindingEntity> mappings(Long id){return fields.findByBindingIdOrderBySequenceNoAsc(id);} public List<BindingFilterConditionEntity> conditions(Long id){return filters.findByBindingIdOrderBySequenceNoAsc(id);}

    private void applyMain(OntologyTableBindingEntity e,SaveBindingRequest r){e.setName(r.name().trim());e.setDataSourceId(r.dataSourceId());e.setSchemaName(r.schemaName()==null?null:r.schemaName().trim());e.setTableName(r.tableName().trim());e.setOntologyId(r.ontologyId());}
    private void saveChildren(Long id,BindingValidationService.ValidatedBinding valid){
        fields.saveAll(valid.mappings().stream().map(m->{OntologyFieldBindingEntity e=new OntologyFieldBindingEntity();e.setBindingId(id);e.setSourceColumn(m.sourceColumn());e.setSourceDataType(m.sourceDataType());e.setOntologyPropertyId(m.property().id());e.setUniqueKey(m.uniqueKey());e.setSequenceNo(m.sequence());return e;}).toList());
        filters.saveAll(valid.filters().stream().map(f->{BindingFilterConditionEntity e=new BindingFilterConditionEntity();e.setBindingId(id);e.setSourceColumn(f.sourceColumn());e.setSourceDataType(f.sourceDataType());e.setOperator(f.operator());e.setTypedValue(f.value());e.setSequenceNo(f.sequence());return e;}).toList());
    }
    private SaveBindingRequest toRequest(OntologyTableBindingEntity e){return new SaveBindingRequest(e.getName(),e.getDataSourceId(),e.getSchemaName(),e.getTableName(),e.getOntologyId(),mappings(e.getId()).stream().map(m->new SaveBindingRequest.FieldMappingItem(m.getSourceColumn(),m.getOntologyPropertyId())).toList(),conditions(e.getId()).stream().map(f->new SaveBindingRequest.FilterItem(f.getSourceColumn(),f.getOperator(),f.getTypedValue())).toList(),e.getVersion());}
    private BindingResponse toResponse(OntologyTableBindingEntity e){
        var source=dataSources.requireEntity(e.getDataSourceId()); OntologySchema schema=schemas.getOntologySchema(e.getOntologyId());Map<Long,PropertyDefinition> properties=new HashMap<>();schema.properties().forEach(p->properties.put(p.id(),p));
        List<BindingResponse.FieldMapping> mapped=mappings(e.getId()).stream().map(m->{PropertyDefinition p=properties.get(m.getOntologyPropertyId());return new BindingResponse.FieldMapping(m.getId(),m.getSourceColumn(),m.getSourceDataType(),m.getOntologyPropertyId(),p==null?null:p.code(),p==null?null:p.name(),m.isUniqueKey());}).toList();
        List<BindingResponse.FilterCondition> condition=conditions(e.getId()).stream().map(f->new BindingResponse.FilterCondition(f.getId(),f.getSourceColumn(),f.getSourceDataType(),f.getOperator(),f.getTypedValue(),f.getSequenceNo())).toList();
        return new BindingResponse(e.getId(),e.getName(),e.getDataSourceId(),source.getName(),e.getSchemaName(),e.getTableName(),e.getOntologyId(),schema.name(),e.getStatus(),e.getLastTestStatus(),e.getLastTestAt(),mapped,condition,e.getVersion(),e.getCreatedAt(),e.getUpdatedAt());
    }
    private void requireDisabled(OntologyTableBindingEntity e){if(e.getStatus()==ConfigStatus.ENABLED)throw new BusinessException(PlatformErrorCode.BINDING_ENABLED_EDIT_FORBIDDEN);} private void requireVersion(Long a,Long b){if(b==null||!Objects.equals(a,b))throw new BusinessException(PlatformErrorCode.OPTIMISTIC_LOCK_CONFLICT);}
}
