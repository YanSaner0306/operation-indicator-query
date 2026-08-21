/**
 * 模块9：本体与外部单表Binding主实体。
 * 功能：保存数据源、物理表、本体、启停状态和最近测试状态，不直接持有本体实体关系。
 * 技术栈：Spring Data JPA、乐观锁、软删除和审计时间字段。
 */
package com.biz.ontology.data.binding.model;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.persistence.VersionedEntity;
import com.biz.ontology.data.model.ConnectionTestStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ontology_table_binding")
public class OntologyTableBindingEntity extends VersionedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=100) private String name;
    @Column(name="data_source_id",nullable=false) private Long dataSourceId;
    @Column(name="schema_name",length=128) private String schemaName;
    @Column(name="table_name",nullable=false,length=128) private String tableName;
    @Column(name="ontology_id",nullable=false) private Long ontologyId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ConfigStatus status;
    @Enumerated(EnumType.STRING) @Column(name="last_test_status",length=20) private ConnectionTestStatus lastTestStatus;
    @Column(name="last_test_at") private LocalDateTime lastTestAt;
    @Column(name="deleted_flag",nullable=false) private boolean deletedFlag;
    public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
    public Long getDataSourceId(){return dataSourceId;} public void setDataSourceId(Long v){dataSourceId=v;}
    public String getSchemaName(){return schemaName;} public void setSchemaName(String v){schemaName=v;}
    public String getTableName(){return tableName;} public void setTableName(String v){tableName=v;}
    public Long getOntologyId(){return ontologyId;} public void setOntologyId(Long v){ontologyId=v;}
    public ConfigStatus getStatus(){return status;} public void setStatus(ConfigStatus v){status=v;}
    public ConnectionTestStatus getLastTestStatus(){return lastTestStatus;} public void setLastTestStatus(ConnectionTestStatus v){lastTestStatus=v;}
    public LocalDateTime getLastTestAt(){return lastTestAt;} public void setLastTestAt(LocalDateTime v){lastTestAt=v;}
    public boolean isDeletedFlag(){return deletedFlag;} public void setDeletedFlag(boolean v){deletedFlag=v;}
}
