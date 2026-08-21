package com.biz.ontology.api.domain;

import com.biz.ontology.api.common.R;
import com.biz.ontology.api.common.PageResponse;
import com.biz.ontology.api.domain.dto.CreateDomainRequest;
import com.biz.ontology.api.domain.dto.CreateParentDomainRequest;
import com.biz.ontology.api.domain.dto.DomainResponse;
import com.biz.ontology.api.domain.dto.DomainTreeNodeResponse;
import com.biz.ontology.api.domain.dto.UpdateDomainRequest;
import com.biz.ontology.api.domain.dto.UpdateDomainStatusRequest;
import com.biz.ontology.domain.enums.DomainStatus;
import com.biz.ontology.domain.service.DomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@Validated
@Tag(name = "领域管理", description = "业务领域创建、查询、编辑、启停和删除")
@RestController
@RequestMapping({"/api/v1/domains", "/api/v1/domain"})
public class DomainController {

    private final DomainService domainService;

    public DomainController(DomainService domainService) {
        this.domainService = domainService;
    }

    @Operation(summary = "创建领域")
    @PostMapping
    public R<DomainResponse> create(@Valid @RequestBody CreateDomainRequest request) {
        return R.ok(domainService.create(request));
    }

    @Operation(summary = "创建父领域并归组已有独立领域")
    @PostMapping("/parents")
    public R<DomainResponse> createParent(@Valid @RequestBody CreateParentDomainRequest request) {
        return R.ok(domainService.createParent(request));
    }

    @Operation(summary = "查询领域树")
    @GetMapping("/tree")
    public R<List<DomainTreeNodeResponse>> tree() {
        return R.ok(domainService.tree());
    }

    @Operation(summary = "分页查询领域")
    @GetMapping
    public R<PageResponse<DomainResponse>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DomainStatus status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "页码不能小于0") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量不能小于1")
            @Max(value = 100, message = "每页数量不能超过100") int size) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );
        return R.ok(PageResponse.from(domainService.page(keyword, status, pageable)));
    }

    @Operation(summary = "根据ID查询领域")
    @GetMapping("/{id}")
    public R<DomainResponse> getById(@PathVariable Long id) {
        return R.ok(domainService.getById(id));
    }

    @Operation(summary = "编辑领域名称和说明")
    @PutMapping("/{id}")
    public R<DomainResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDomainRequest request) {
        return R.ok(domainService.update(id, request));
    }

    @Operation(summary = "更新领域状态")
    @PatchMapping("/{id}/status")
    public R<DomainResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDomainStatusRequest request) {
        return R.ok(domainService.updateStatus(id, request.getStatus()));
    }

    @Operation(summary = "启用领域")
    @PatchMapping("/{id}/enable")
    public R<DomainResponse> enable(@PathVariable Long id) {
        return R.ok(domainService.updateStatus(id, DomainStatus.ENABLED));
    }

    @Operation(summary = "禁用领域")
    @PatchMapping("/{id}/disable")
    public R<DomainResponse> disable(@PathVariable Long id) {
        return R.ok(domainService.updateStatus(id, DomainStatus.DISABLED));
    }

    @Operation(summary = "删除未被本体关联的领域")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        domainService.delete(id);
        return R.ok();
    }

    @GetMapping("/ping")
    public R<Map<String, String>> ping() {
        return R.ok(Map.of("module", "domain", "status", "ready"));
    }
}
