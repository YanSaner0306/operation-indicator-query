/**
 * 模块7：外部JDBC连接创建抽象。
 * 功能：隔离真实网络连接，便于对连接超时、只读校验和异常路径做纯单元测试。
 * 技术栈：Java JDBC与依赖倒置接口。
 */
package com.biz.ontology.data.connection;

import com.biz.ontology.data.model.DataSourceConfigEntity;
import java.sql.Connection;
import java.sql.SQLException;

public interface ExternalJdbcConnectionFactory {
    Connection open(DataSourceConfigEntity config, String plaintextPassword) throws SQLException;
}
