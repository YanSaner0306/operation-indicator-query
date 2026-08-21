/**
 * 模块9-10：Binding字段映射实体。
 * 功能：保存可信源字段、数据库类型、本体属性和唯一键标志，保证单个Binding内双向唯一。
 * 技术栈：Spring Data JPA实体映射与数据库唯一约束。
 */
package com.biz.ontology.data.binding.model;

import jakarta.persistence.*;

@Entity
@Table(name="ontology_field_binding")
public class OntologyFieldBindingEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="binding_id",nullable=false) private Long bindingId;
    @Column(name="source_column",nullable=false,length=128) private String sourceColumn;
    @Column(name="source_data_type",nullable=false,length=100) private String sourceDataType;
    @Column(name="ontology_property_id",nullable=false) private Long ontologyPropertyId;
    @Column(name="unique_key",nullable=false) private boolean uniqueKey;
    @Column(name="sequence_no",nullable=false) private Integer sequenceNo;
    public Long getId(){return id;} public Long getBindingId(){return bindingId;} public void setBindingId(Long v){bindingId=v;}
    public String getSourceColumn(){return sourceColumn;} public void setSourceColumn(String v){sourceColumn=v;}
    public String getSourceDataType(){return sourceDataType;} public void setSourceDataType(String v){sourceDataType=v;}
    public Long getOntologyPropertyId(){return ontologyPropertyId;} public void setOntologyPropertyId(Long v){ontologyPropertyId=v;}
    public boolean isUniqueKey(){return uniqueKey;} public void setUniqueKey(boolean v){uniqueKey=v;}
    public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer v){sequenceNo=v;}
}
