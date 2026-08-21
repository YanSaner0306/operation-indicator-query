/**
 * 模块6-7：数据源最近一次连接测试状态。
 * 功能：区分尚未测试、测试成功和测试失败三种状态。
 * 技术栈：Java 17枚举与JPA字符串枚举映射。
 */
package com.biz.ontology.data.model;

public enum ConnectionTestStatus {
    UNTESTED,
    SUCCESS,
    FAILED
}
