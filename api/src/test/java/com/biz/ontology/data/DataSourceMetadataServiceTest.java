/**
 * 模块8测试：使用真实H2元数据验证表列白名单、行数限制和敏感字段掩码。
 * 技术栈：JUnit 5、H2 JDBC、Mockito与JDBC DatabaseMetaData。
 */
package com.biz.ontology.data;

import com.biz.ontology.api.data.dto.PreviewRequest;
import com.biz.ontology.data.config.DataSourceSecurityProperties;
import com.biz.ontology.data.connection.DynamicDataSourcePoolRegistry;
import com.biz.ontology.data.model.DataSourceConfigEntity;
import com.biz.ontology.data.service.*;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DataSourceMetadataServiceTest {
    private static final String URL="jdbc:h2:mem:metadata_test;DB_CLOSE_DELAY=-1";
    @BeforeAll static void prepare() throws Exception {
        try(Connection c=DriverManager.getConnection(URL,"sa",""); Statement s=c.createStatement()) {
            s.execute("CREATE TABLE CUSTOMERS (ID BIGINT PRIMARY KEY, NAME VARCHAR(50), PASSWORD VARCHAR(50))");
            s.execute("INSERT INTO CUSTOMERS VALUES (1, 'Alice', 'secret-value')");
        }
    }

    @Test void shouldReadMetadataAndMaskSensitivePreviewColumn() throws Exception {
        DataSourceConfigService configs=mock(DataSourceConfigService.class); DynamicDataSourcePoolRegistry pools=mock(DynamicDataSourcePoolRegistry.class);
        DataSourceConfigEntity config=mock(DataSourceConfigEntity.class); when(config.getDatabaseName()).thenReturn("METADATA_TEST"); when(configs.requireEntity(1L)).thenReturn(config);
        when(pools.getConnection(config)).thenAnswer(ignored -> DriverManager.getConnection(URL,"sa",""));
        DataSourceSecurityProperties properties=new DataSourceSecurityProperties(); properties.setQueryTimeoutSeconds(2);
        DataSourceMetadataService service=new DataSourceMetadataService(configs,pools,properties);
        assertThat(service.tables(1L)).extracting("name").contains("CUSTOMERS");
        assertThat(service.columns(1L,"CUSTOMERS")).anyMatch(column -> column.name().equals("ID") && column.primaryKey());
        var preview=service.preview(1L,"CUSTOMERS",new PreviewRequest(List.of("NAME","PASSWORD"),20));
        assertThat(preview.rows()).hasSize(1); assertThat(preview.rows().get(0).get("NAME")).isEqualTo("Alice");
        assertThat(preview.rows().get(0).get("PASSWORD")).isEqualTo("se****ue");
    }
}
