/**
 * 模块9-11：Binding结构化筛选条件实体。
 * 功能：保存元数据白名单字段、操作符和规范化值，运行时只通过PreparedStatement绑定。
 * 技术栈：Spring Data JPA与字符串枚举映射。
 */
package com.biz.ontology.data.binding.model;

import jakarta.persistence.*;

@Entity
@Table(name="binding_filter_condition")
public class BindingFilterConditionEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="binding_id",nullable=false) private Long bindingId;
    @Column(name="source_column",nullable=false,length=128) private String sourceColumn;
    @Column(name="source_data_type",nullable=false,length=100) private String sourceDataType;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private BindingFilterOperator operator;
    @Column(name="typed_value",length=2000) private String typedValue;
    @Column(name="sequence_no",nullable=false) private Integer sequenceNo;
    public Long getId(){return id;} public Long getBindingId(){return bindingId;} public void setBindingId(Long v){bindingId=v;}
    public String getSourceColumn(){return sourceColumn;} public void setSourceColumn(String v){sourceColumn=v;}
    public String getSourceDataType(){return sourceDataType;} public void setSourceDataType(String v){sourceDataType=v;}
    public BindingFilterOperator getOperator(){return operator;} public void setOperator(BindingFilterOperator v){operator=v;}
    public String getTypedValue(){return typedValue;} public void setTypedValue(String v){typedValue=v;}
    public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer v){sequenceNo=v;}
}
