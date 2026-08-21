/**
 * 模块7测试：验证MySQL只读授权检查会拒绝包含写权限的账号。
 * 技术栈：JUnit 5、Mockito、JDBC接口模拟与AssertJ。
 */
package com.biz.ontology.data;

import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.data.config.DataSourceSecurityProperties;
import com.biz.ontology.data.connection.*;
import com.biz.ontology.data.model.*;
import com.biz.ontology.data.repository.*;
import com.biz.ontology.data.security.PasswordEncryptionService;
import com.biz.ontology.data.service.DataSourceConfigService;
import org.junit.jupiter.api.Test;
import java.sql.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DataSourceConnectionTestServiceTest {
    @Test void shouldRejectMysqlAccountWithWriteGrant() throws Exception {
        DataSourceConfigService configs=mock(DataSourceConfigService.class); DataSourceConfigRepository repository=mock(DataSourceConfigRepository.class);
        DataSourceTestLogRepository logs=mock(DataSourceTestLogRepository.class); PasswordEncryptionService encryption=mock(PasswordEncryptionService.class);
        ExternalJdbcConnectionFactory factory=mock(ExternalJdbcConnectionFactory.class); DataSourceSecurityProperties properties=new DataSourceSecurityProperties();
        DataSourceConfigEntity config=mock(DataSourceConfigEntity.class); when(config.getId()).thenReturn(7L); when(config.getDbType()).thenReturn(DatabaseType.MYSQL);
        when(configs.requireEntity(7L)).thenReturn(config); when(encryption.decrypt(any(),any())).thenReturn("secret");
        Connection connection=mock(Connection.class); Statement select=mock(Statement.class); Statement grants=mock(Statement.class);
        ResultSet selectRows=mock(ResultSet.class); ResultSet grantRows=mock(ResultSet.class);
        when(factory.open(config,"secret")).thenReturn(connection); when(connection.createStatement()).thenReturn(select, grants);
        when(select.executeQuery("SELECT 1")).thenReturn(selectRows); when(grants.executeQuery("SHOW GRANTS FOR CURRENT_USER()" )).thenReturn(grantRows);
        when(grantRows.next()).thenReturn(true,false); when(grantRows.getString(1)).thenReturn("GRANT SELECT, INSERT ON orders.* TO reader");
        DataSourceConnectionTestService service=new DataSourceConnectionTestService(configs,repository,logs,encryption,factory,properties);
        assertThatThrownBy(() -> service.test(7L)).isInstanceOf(BusinessException.class).hasMessageContaining("只读");
        verify(logs).save(any(DataSourceTestLogEntity.class));
    }
}
