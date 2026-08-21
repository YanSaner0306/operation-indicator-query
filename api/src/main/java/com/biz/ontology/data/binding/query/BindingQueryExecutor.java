/**
 * 模块11-13：Binding只读查询执行器。
 * 功能：从受限连接池执行参数化SELECT，设置查询超时和最大行数，并按映射字段返回有序结果。
 * 技术栈：JDBC PreparedStatement、HikariCP与Spring配置属性。
 */
package com.biz.ontology.data.binding.query;

import com.biz.ontology.data.binding.model.*;
import com.biz.ontology.data.config.DataSourceSecurityProperties;
import com.biz.ontology.data.connection.DynamicDataSourcePoolRegistry;
import com.biz.ontology.data.model.DataSourceConfigEntity;
import org.springframework.stereotype.Component;
import java.sql.*;
import java.util.*;

@Component
public class BindingQueryExecutor {
    private final DynamicDataSourcePoolRegistry pools; private final ControlledSelectBuilder builder; private final DataSourceSecurityProperties properties;
    public BindingQueryExecutor(DynamicDataSourcePoolRegistry a,ControlledSelectBuilder b,DataSourceSecurityProperties c){pools=a;builder=b;properties=c;}
    public Optional<Map<String,Object>> queryOne(DataSourceConfigEntity source,OntologyTableBindingEntity binding,List<OntologyFieldBindingEntity> mappings,List<BindingFilterConditionEntity> filters,String keyColumn,Object key) throws SQLException {
        return queryMany(source,binding,mappings,filters,keyColumn,key,1).stream().findFirst();
    }
    public List<Map<String,Object>> queryMany(DataSourceConfigEntity source,OntologyTableBindingEntity binding,List<OntologyFieldBindingEntity> mappings,List<BindingFilterConditionEntity> filters,String keyColumn,Object key,int limit) throws SQLException {
        try(Connection connection=pools.getConnection(source)){
            String quote=Optional.ofNullable(connection.getMetaData().getIdentifierQuoteString()).orElse("");
            int safeLimit=Math.max(1,Math.min(limit,100));
            ControlledSelectBuilder.QueryPlan plan=builder.build(quote,binding,mappings,filters,keyColumn,key,safeLimit);
            try(PreparedStatement statement=connection.prepareStatement(plan.sql())){
                statement.setQueryTimeout(properties.getQueryTimeoutSeconds());statement.setMaxRows(safeLimit);
                for(int i=0;i<plan.parameters().size();i++)statement.setObject(i+1,plan.parameters().get(i));
                try(ResultSet rows=statement.executeQuery()){
                    List<Map<String,Object>> results=new ArrayList<>();
                    while(rows.next()){
                        Map<String,Object> result=new LinkedHashMap<>();
                        for(OntologyFieldBindingEntity mapping:mappings)result.put(mapping.getSourceColumn(),rows.getObject(mapping.getSourceColumn()));
                        results.add(result);
                    }
                    return List.copyOf(results);
                }
            }
        }
    }
}
