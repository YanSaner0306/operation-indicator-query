/**
 * 模块6：受支持的外部数据库类型。
 * 功能：限制连接字符串只能由平台按白名单数据库类型生成，避免用户注入任意JDBC URL。
 * 技术栈：Java 17枚举。
 */
package com.biz.ontology.data.model;

public enum DatabaseType {
    MYSQL,
    H2
}
