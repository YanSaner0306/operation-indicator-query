/**
 * 模块7：数据源连通性与只读权限测试服务。
 * 功能：验证连接、执行轻量查询、检查MySQL授权是否含写权限，并保存脱敏测试日志。
 * 技术栈：Spring事务、JDBC DatabaseMetaData/SHOW GRANTS与AES-GCM解密。
 */
package com.biz.ontology.data.connection;

import com.biz.ontology.api.data.dto.ConnectionTestResponse;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.data.config.DataSourceSecurityProperties;
import com.biz.ontology.data.model.*;
import com.biz.ontology.data.repository.*;
import com.biz.ontology.data.security.PasswordEncryptionService;
import com.biz.ontology.data.service.DataSourceConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class DataSourceConnectionTestService {
    private static final Pattern WRITE_GRANT = Pattern.compile("\\b(ALL PRIVILEGES|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP|TRIGGER|EXECUTE|GRANT OPTION|FILE|PROCESS|RELOAD|SHUTDOWN|SUPER)\\b");
    private final DataSourceConfigService configService; private final DataSourceConfigRepository configRepository;
    private final DataSourceTestLogRepository logRepository; private final PasswordEncryptionService encryptionService;
    private final ExternalJdbcConnectionFactory connectionFactory; private final DataSourceSecurityProperties properties;
    public DataSourceConnectionTestService(DataSourceConfigService a, DataSourceConfigRepository b, DataSourceTestLogRepository c,
                                           PasswordEncryptionService d, ExternalJdbcConnectionFactory e, DataSourceSecurityProperties f) {
        configService=a; configRepository=b; logRepository=c; encryptionService=d; connectionFactory=e; properties=f;
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public ConnectionTestResponse test(Long id) {
        DataSourceConfigEntity config = configService.requireEntity(id); long started = System.nanoTime();
        try (Connection connection = connectionFactory.open(config, encryptionService.decrypt(config.getPasswordCipher(), config.getPasswordIv()))) {
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(properties.getQueryTimeoutSeconds());
                try (ResultSet ignored = statement.executeQuery("SELECT 1")) { }
            }
            if (config.getDbType() == DatabaseType.MYSQL) verifyMysqlReadOnly(connection);
            return finish(config, true, started, "连接成功，账号通过只读权限检查");
        } catch (BusinessException exception) {
            finish(config, false, started, exception.getMessage()); throw exception;
        } catch (SQLException exception) {
            finish(config, false, started, sanitize(exception));
            throw new BusinessException(PlatformErrorCode.DATASOURCE_CONNECT_FAILED, "数据源连接失败：" + sanitize(exception));
        }
    }

    private void verifyMysqlReadOnly(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SHOW GRANTS FOR CURRENT_USER()")) {
            while (rows.next()) {
                String grant = rows.getString(1).toUpperCase(Locale.ROOT);
                if (WRITE_GRANT.matcher(grant).find()) throw new BusinessException(PlatformErrorCode.DATASOURCE_NOT_READ_ONLY);
            }
        }
    }

    private ConnectionTestResponse finish(DataSourceConfigEntity config, boolean success, long started, String message) {
        long latency = (System.nanoTime() - started) / 1_000_000;
        config.setLastTestStatus(success ? ConnectionTestStatus.SUCCESS : ConnectionTestStatus.FAILED); config.setLastTestAt(LocalDateTime.now());
        configRepository.save(config);
        DataSourceTestLogEntity log = new DataSourceTestLogEntity(); log.setDataSourceId(config.getId()); log.setSuccess(success);
        log.setMessage(message.length() > 500 ? message.substring(0, 500) : message); log.setLatencyMs(latency); log.setTestedAt(LocalDateTime.now()); logRepository.save(log);
        return new ConnectionTestResponse(success, latency, message);
    }

    private String sanitize(SQLException exception) {
        String state = exception.getSQLState();
        return state == null ? "JDBC错误" : "SQLState=" + state;
    }
}
