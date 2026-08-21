/**
 * 模块7：按配置动态管理外部数据源连接池。
 * 功能：为启用的数据源惰性创建小型只读Hikari连接池，配置变化、停用或删除时立即关闭旧池。
 * 技术栈：HikariCP、ConcurrentHashMap、JDBC和Jakarta生命周期回调。
 */
package com.biz.ontology.data.connection;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.data.config.DataSourceSecurityProperties;
import com.biz.ontology.data.model.DataSourceConfigEntity;
import com.biz.ontology.data.security.HostAccessPolicy;
import com.biz.ontology.data.security.PasswordEncryptionService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicDataSourcePoolRegistry {
    private final Map<Long, PoolHolder> pools = new ConcurrentHashMap<>();
    private final DriverManagerConnectionFactory urlFactory; private final PasswordEncryptionService encryption;
    private final HostAccessPolicy hostPolicy; private final DataSourceSecurityProperties properties;
    public DynamicDataSourcePoolRegistry(DriverManagerConnectionFactory a, PasswordEncryptionService b, HostAccessPolicy c, DataSourceSecurityProperties d) {
        urlFactory=a; encryption=b; hostPolicy=c; properties=d;
    }

    public Connection getConnection(DataSourceConfigEntity config) throws SQLException {
        if (config.getStatus() != ConfigStatus.ENABLED) throw new BusinessException(PlatformErrorCode.DATASOURCE_CONFIG_INVALID, "数据源尚未启用");
        hostPolicy.validate(config.getHost());
        String fingerprint = config.getVersion() + ":" + config.getUpdatedAt();
        PoolHolder holder = pools.compute(config.getId(), (id, current) -> {
            if (current != null && current.fingerprint.equals(fingerprint)) return current;
            if (current != null) current.pool.close();
            return new PoolHolder(fingerprint, create(config));
        });
        return holder.pool.getConnection();
    }

    public void evict(Long id) {
        PoolHolder holder = pools.remove(id);
        if (holder != null) holder.pool.close();
    }

    private HikariDataSource create(DataSourceConfigEntity config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(urlFactory.jdbcUrl(config)); hikari.setUsername(config.getUsername());
        hikari.setPassword(encryption.decrypt(config.getPasswordCipher(), config.getPasswordIv()));
        hikari.setReadOnly(true); hikari.setMaximumPoolSize(properties.getMaxPoolSize()); hikari.setMinimumIdle(0);
        hikari.setConnectionTimeout(properties.getConnectTimeoutMs()); hikari.setValidationTimeout(Math.min(properties.getConnectTimeoutMs(), 3000));
        hikari.setPoolName("external-ds-" + config.getId());
        return new HikariDataSource(hikari);
    }

    @PreDestroy
    public void closeAll() { pools.values().forEach(holder -> holder.pool.close()); pools.clear(); }
    private record PoolHolder(String fingerprint, HikariDataSource pool) {}
}
