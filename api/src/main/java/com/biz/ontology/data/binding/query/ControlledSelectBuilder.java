/**
 * 模块11-13：Binding受控SELECT构建器。
 * 功能：仅使用已持久化的元数据标识符生成SELECT/WHERE/LIMIT，并把全部业务值留作参数绑定。
 * 技术栈：Java字符串构建、JDBC标识符引用和PreparedStatement占位符。
 */
package com.biz.ontology.data.binding.query;

import com.biz.ontology.data.binding.model.*;
import com.biz.ontology.data.binding.validation.FilterValueCodec;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class ControlledSelectBuilder {
    public QueryPlan build(String quote,OntologyTableBindingEntity binding,List<OntologyFieldBindingEntity> mappings,
                           List<BindingFilterConditionEntity> filters,String keyColumn,Object businessKey,int limit) {
        String q=quote==null?"":quote.trim(); String table=identifier(q,binding.getTableName());
        String selected=mappings.stream().map(m->identifier(q,m.getSourceColumn())).reduce((a,b)->a+","+b).orElseThrow();
        List<String> predicates=new ArrayList<>(); List<Object> parameters=new ArrayList<>();
        for(BindingFilterConditionEntity filter:filters) appendFilter(q,filter,predicates,parameters);
        if(keyColumn!=null){predicates.add(identifier(q,keyColumn)+" = ?");parameters.add(businessKey);}
        String sql="SELECT "+selected+" FROM "+table+(predicates.isEmpty()?"":" WHERE "+String.join(" AND ",predicates))+" LIMIT "+Math.max(1,Math.min(limit,100));
        return new QueryPlan(sql,List.copyOf(parameters));
    }
    private void appendFilter(String quote,BindingFilterConditionEntity filter,List<String> predicates,List<Object> parameters){
        String column=identifier(quote,filter.getSourceColumn());
        switch(filter.getOperator()){
            case IS_NULL -> predicates.add(column+" IS NULL"); case NOT_NULL -> predicates.add(column+" IS NOT NULL");
            case IN -> {List<Object> values=FilterValueCodec.parseMany(filter.getTypedValue(),filter.getSourceDataType());predicates.add(column+" IN ("+String.join(",",Collections.nCopies(values.size(),"?"))+")");parameters.addAll(values);}
            default -> {String operator=switch(filter.getOperator()){case EQ->"=";case NE->"<>";case GT->">";case GE->">=";case LT->"<";case LE->"<=";default->throw new IllegalStateException();};predicates.add(column+" "+operator+" ?");parameters.add(FilterValueCodec.parseOne(filter.getTypedValue(),filter.getSourceDataType()));}
        }
    }
    private String identifier(String quote,String value){return quote+value+quote;}
    public record QueryPlan(String sql,List<Object> parameters){}
}
