/**
 * 模块11-12：Binding测试预览服务。
 * 功能：执行LIMIT 1参数化查询、转换本体属性、更新最近测试状态并写入不含SQL的测试日志。
 * 技术栈：Spring事务、JDBC查询执行器、requestId上下文与跨模块本体Schema合同。
 */
package com.biz.ontology.data.binding.service;

import com.biz.ontology.api.data.dto.BindingPreviewResponse;
import com.biz.ontology.common.exception.*;
import com.biz.ontology.common.web.RequestIdContext;
import com.biz.ontology.data.binding.model.*;
import com.biz.ontology.data.binding.query.BindingQueryExecutor;
import com.biz.ontology.data.binding.repository.*;
import com.biz.ontology.data.model.*;
import com.biz.ontology.data.security.SensitiveValueMasker;
import com.biz.ontology.data.service.DataSourceConfigService;
import com.biz.ontology.ontology.query.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BindingPreviewService {
    private final BindingService service;private final DataSourceConfigService sources;private final BindingQueryExecutor executor;
    private final OntologySchemaQueryService schemas;private final OntologyTableBindingRepository bindings;private final BindingTestLogRepository logs;
    public BindingPreviewService(BindingService a,DataSourceConfigService b,BindingQueryExecutor c,OntologySchemaQueryService d,OntologyTableBindingRepository e,BindingTestLogRepository f){service=a;sources=b;executor=c;schemas=d;bindings=e;logs=f;}
    @Transactional(noRollbackFor=BusinessException.class)
    public BindingPreviewResponse preview(Long id){
        OntologyTableBindingEntity binding=service.require(id);List<OntologyFieldBindingEntity> mappings=service.mappings(id);List<BindingFilterConditionEntity> filters=service.conditions(id);
        DataSourceConfigEntity source=sources.requireEntity(binding.getDataSourceId());long started=System.nanoTime();
        try{
            Map<String,Object> row=executor.queryOne(source,binding,mappings,filters,null,null).orElseThrow(()->new BusinessException(PlatformErrorCode.BINDING_PREVIEW_FAILED,"筛选条件下没有可预览数据"));
            OntologySchema schema=schemas.getOntologySchema(binding.getOntologyId());Map<Long,PropertyDefinition> properties=new HashMap<>();schema.properties().forEach(p->properties.put(p.id(),p));
            Map<String,Object> safeSourceValues=new LinkedHashMap<>();Map<String,Object> converted=new LinkedHashMap<>();Object externalKey=null;
            for(OntologyFieldBindingEntity mapping:mappings){PropertyDefinition property=properties.get(mapping.getOntologyPropertyId());if(property!=null){Object value=row.get(mapping.getSourceColumn());boolean sensitive=SensitiveValueMasker.isSensitive(mapping.getSourceColumn())||SensitiveValueMasker.isSensitive(property.code())||SensitiveValueMasker.isSensitive(property.name());Object safeValue=sensitive?SensitiveValueMasker.mask(value):value;safeSourceValues.put(mapping.getSourceColumn(),safeValue);converted.put(property.code(),safeValue);if(mapping.isUniqueKey())externalKey=safeValue;}}
            long duration=elapsed(started);finish(binding,true,duration,null,"预览成功");return new BindingPreviewResponse(binding.getOntologyId(),externalKey,safeSourceValues,converted,duration);
        }catch(BusinessException exception){finish(binding,false,elapsed(started),exception.getErrorCode().getResponseCode(),exception.getMessage());throw exception;}
        catch(SQLException exception){finish(binding,false,elapsed(started),PlatformErrorCode.BINDING_PREVIEW_FAILED.getResponseCode(),"外部查询失败");throw new BusinessException(PlatformErrorCode.BINDING_PREVIEW_FAILED);}
    }
    private void finish(OntologyTableBindingEntity binding,boolean success,long duration,String error,String message){binding.setLastTestStatus(success?ConnectionTestStatus.SUCCESS:ConnectionTestStatus.FAILED);binding.setLastTestAt(LocalDateTime.now());bindings.save(binding);BindingTestLogEntity log=new BindingTestLogEntity();log.setBindingId(binding.getId());log.setSuccess(success);log.setDurationMs(duration);log.setErrorCode(error);log.setMessage(message.length()>500?message.substring(0,500):message);log.setRequestId(RequestIdContext.current());log.setTestedAt(LocalDateTime.now());logs.save(log);}
    private long elapsed(long started){return(System.nanoTime()-started)/1_000_000;}
}
