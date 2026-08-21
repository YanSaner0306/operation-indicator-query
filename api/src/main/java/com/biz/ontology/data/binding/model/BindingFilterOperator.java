/**
 * 模块9-11：Binding结构化筛选操作符白名单。
 * 功能：限定P0只支持AND组合下的比较、集合和空值判断，杜绝任意SQL操作符输入。
 * 技术栈：Java 17枚举与JDBC参数化查询。
 */
package com.biz.ontology.data.binding.model;

public enum BindingFilterOperator {
    EQ, NE, GT, GE, LT, LE, IN, IS_NULL, NOT_NULL
}
