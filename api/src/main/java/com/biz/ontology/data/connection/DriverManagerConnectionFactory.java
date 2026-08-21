/**
 * 模块7：受控JDBC连接工厂实现。
 * 功能：仅按平台支持的数据库类型拼接JDBC URL，并在连接前执行SSRF地址复核和超时设置。
 * 技术栈：JDBC DriverManager、MySQL/H2驱动与SSRF策略。
 */
package com.biz.ontology.data.connection;

import com.biz.ontology.data.config.DataSourceSecurityProperties;
import com.biz.ontology.data.model.DataSourceConfigEntity;
import com.biz.ontology.data.security.HostAccessPolicy;
import org.springframework.stereotype.Component;
import java.sql.*;
import java.util.Properties;

@Component
public class DriverManagerConnectionFactory implements ExternalJdbcConnectionFactory {
    private final HostAccessPolicy hostPolicy;
    private final DataSourceSecurityProperties properties;
    public DriverManagerConnectionFactory(HostAccessPolicy hostPolicy, DataSourceSecurityProperties properties) { this.hostPolicy = hostPolicy; this.properties = properties; }

    @Override
    public Connection open(DataSourceConfigEntity config, String password) throws SQLException {
        hostPolicy.validate(config.getHost());
        Properties values = new Properties();
        values.setProperty("user", config.getUsername()); values.setProperty("password", password);
        values.setProperty("connectTimeout", String.valueOf(properties.getConnectTimeoutMs()));
        values.setProperty("socketTimeout", String.valueOf(properties.getQueryTimeoutSeconds() * 1000));
        Connection connection = DriverManager.getConnection(jdbcUrl(config), values);
        connection.setReadOnly(true);
        return connection;
    }

    public String jdbcUrl(DataSourceConfigEntity config) {
        return switch (config.getDbType()) {
            case MYSQL -> "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabaseName()
                    + "?useSSL=true&allowPublicKeyRetrieval=false&serverTimezone=UTC";
            case H2 -> "jdbc:h2:tcp://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabaseName();
        };
    }
}
