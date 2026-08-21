/**
 * 模块8：外部数据源元数据与安全预览服务。
 * 功能：从DatabaseMetaData建立表列白名单，只生成SELECT语句，限制100行并掩码敏感字段。
 * 技术栈：JDBC DatabaseMetaData、只读Hikari连接池和参数受控SQL生成。
 */
package com.biz.ontology.data.service;

import com.biz.ontology.api.data.dto.*;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.data.config.DataSourceSecurityProperties;
import com.biz.ontology.data.connection.DynamicDataSourcePoolRegistry;
import com.biz.ontology.data.model.DataSourceConfigEntity;
import com.biz.ontology.data.security.SensitiveValueMasker;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;

@Service
public class DataSourceMetadataService {
    private final DataSourceConfigService configService; private final DynamicDataSourcePoolRegistry pools;
    private final DataSourceSecurityProperties properties;
    public DataSourceMetadataService(DataSourceConfigService a, DynamicDataSourcePoolRegistry b, DataSourceSecurityProperties c) { configService=a; pools=b; properties=c; }

    public List<TableMetadataResponse> tables(Long id) {
        DataSourceConfigEntity config = configService.requireEntity(id);
        try (Connection connection = pools.getConnection(config); ResultSet rows = connection.getMetaData().getTables(config.getDatabaseName(), null, "%", new String[]{"TABLE", "VIEW"})) {
            List<TableMetadataResponse> result = new ArrayList<>();
            while (rows.next()) result.add(new TableMetadataResponse(rows.getString("TABLE_NAME"), rows.getString("TABLE_TYPE"), rows.getString("REMARKS")));
            return result.stream().sorted(Comparator.comparing(TableMetadataResponse::name)).toList();
        } catch (SQLException exception) { throw metadataFailure(); }
    }

    public List<ColumnMetadataResponse> columns(Long id, String table) {
        DataSourceConfigEntity config = configService.requireEntity(id);
        try (Connection connection = pools.getConnection(config)) { return loadColumns(connection, config, requireTable(connection, config, table)); }
        catch (SQLException exception) { throw metadataFailure(); }
    }

    public PreviewResponse preview(Long id, String table, PreviewRequest request) {
        DataSourceConfigEntity config = configService.requireEntity(id);
        int limit = request.limit() == null ? 20 : Math.min(request.limit(), 100);
        try (Connection connection = pools.getConnection(config)) {
            String actualTable = requireTable(connection, config, table);
            List<ColumnMetadataResponse> metadata = loadColumns(connection, config, actualTable);
            Map<String, ColumnMetadataResponse> allowed = new LinkedHashMap<>();
            metadata.forEach(column -> allowed.put(column.name(), column));
            List<String> selected = request.columns() == null || request.columns().isEmpty() ? new ArrayList<>(allowed.keySet()) : request.columns();
            if (selected.isEmpty() || selected.stream().anyMatch(column -> !allowed.containsKey(column))) throw new BusinessException(PlatformErrorCode.DATASOURCE_CONFIG_INVALID, "预览列不在元数据白名单中");
            String quote = Optional.ofNullable(connection.getMetaData().getIdentifierQuoteString()).orElse("").trim();
            String sql = "SELECT " + selected.stream().map(value -> quote + value + quote).reduce((a,b) -> a + "," + b).orElseThrow()
                    + " FROM " + quote + actualTable + quote + " LIMIT " + limit;
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(properties.getQueryTimeoutSeconds()); statement.setMaxRows(limit);
                try (ResultSet rows = statement.executeQuery(sql)) {
                    List<Map<String,Object>> values = new ArrayList<>();
                    while (rows.next()) {
                        Map<String,Object> row = new LinkedHashMap<>();
                        for (String column : selected) row.put(column, SensitiveValueMasker.isSensitive(column) ? SensitiveValueMasker.mask(rows.getObject(column)) : rows.getObject(column));
                        values.add(row);
                    }
                    return new PreviewResponse(List.copyOf(selected), values, limit);
                }
            }
        } catch (BusinessException exception) { throw exception; }
        catch (SQLException exception) { throw metadataFailure(); }
    }

    private String requireTable(Connection connection, DataSourceConfigEntity config, String requested) throws SQLException {
        try (ResultSet rows = connection.getMetaData().getTables(config.getDatabaseName(), null, "%", new String[]{"TABLE", "VIEW"})) {
            while (rows.next()) if (rows.getString("TABLE_NAME").equals(requested)) return requested;
        }
        throw new BusinessException(PlatformErrorCode.DATASOURCE_TABLE_NOT_FOUND);
    }

    private List<ColumnMetadataResponse> loadColumns(Connection connection, DataSourceConfigEntity config, String table) throws SQLException {
        Set<String> primary = new HashSet<>();
        try (ResultSet keys = connection.getMetaData().getPrimaryKeys(config.getDatabaseName(), null, table)) { while (keys.next()) primary.add(keys.getString("COLUMN_NAME")); }
        List<ColumnMetadataResponse> result = new ArrayList<>();
        try (ResultSet rows = connection.getMetaData().getColumns(config.getDatabaseName(), null, table, "%")) {
            while (rows.next()) { String name=rows.getString("COLUMN_NAME"); result.add(new ColumnMetadataResponse(name, rows.getString("TYPE_NAME"), rows.getInt("DATA_TYPE"), rows.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls, primary.contains(name))); }
        }
        return result;
    }

    private BusinessException metadataFailure() { return new BusinessException(PlatformErrorCode.DATASOURCE_METADATA_FAILED); }
}
