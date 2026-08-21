/**
 * 模块6-8：数据源配置、连接测试、元数据和数据预览REST接口。
 * 功能：提供受RBAC保护的CRUD、启停、测试、表列读取和限量预览端点。
 * 技术栈：Spring MVC、Bean Validation、Spring Method Security与统一响应封装。
 */
package com.biz.ontology.api.data;

import com.biz.ontology.api.common.*;
import com.biz.ontology.api.data.dto.*;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.data.connection.DataSourceConnectionTestService;
import com.biz.ontology.data.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/data-sources")
public class DataSourceController {
    private final DataSourceConfigService configService; private final DataSourceConnectionTestService testService;
    private final DataSourceMetadataService metadataService;
    public DataSourceController(DataSourceConfigService a, DataSourceConnectionTestService b, DataSourceMetadataService c) { configService=a; testService=b; metadataService=c; }

    @GetMapping @PreAuthorize("hasAuthority('DATASOURCE_VIEW')")
    public R<PageResponse<DataSourceResponse>> page(@RequestParam(required=false) String keyword,
            @RequestParam(required=false) ConfigStatus status, @RequestParam(defaultValue="1") @Min(1) int page,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        return R.ok(configService.page(keyword, status, page, size));
    }
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('DATASOURCE_VIEW')")
    public R<DataSourceResponse> get(@PathVariable Long id) { return R.ok(configService.get(id)); }
    @PostMapping @PreAuthorize("hasAuthority('DATASOURCE_MANAGE')")
    public R<DataSourceResponse> create(@Valid @RequestBody SaveDataSourceRequest request) { return R.ok(configService.create(request)); }
    @PutMapping("/{id}") @PreAuthorize("hasAuthority('DATASOURCE_MANAGE')")
    public R<DataSourceResponse> update(@PathVariable Long id, @Valid @RequestBody SaveDataSourceRequest request) { return R.ok(configService.update(id, request)); }
    @PatchMapping("/{id}/enabled") @PreAuthorize("hasAuthority('DATASOURCE_MANAGE')")
    public R<DataSourceResponse> status(@PathVariable Long id, @Valid @RequestBody UpdateDataSourceStatusRequest request) { return R.ok(configService.updateStatus(id, request)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('DATASOURCE_MANAGE')")
    public R<Void> delete(@PathVariable Long id, @RequestParam @NotNull Long version) { configService.delete(id, version); return R.ok(); }
    @PostMapping("/{id}/test") @PreAuthorize("hasAuthority('DATASOURCE_MANAGE')")
    public R<ConnectionTestResponse> test(@PathVariable Long id) { return R.ok(testService.test(id)); }
    @GetMapping("/{id}/tables") @PreAuthorize("hasAuthority('DATASOURCE_VIEW')")
    public R<List<TableMetadataResponse>> tables(@PathVariable Long id) { return R.ok(metadataService.tables(id)); }
    @GetMapping("/{id}/tables/{table}/columns") @PreAuthorize("hasAuthority('DATASOURCE_VIEW')")
    public R<List<ColumnMetadataResponse>> columns(@PathVariable Long id, @PathVariable String table) { return R.ok(metadataService.columns(id, table)); }
    @PostMapping("/{id}/tables/{table}/preview") @PreAuthorize("hasAuthority('DATASOURCE_VIEW')")
    public R<PreviewResponse> preview(@PathVariable Long id, @PathVariable String table, @Valid @RequestBody PreviewRequest request) { return R.ok(metadataService.preview(id, table, request)); }
}
