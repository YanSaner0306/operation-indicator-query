package com.biz.ontology.api.ontology;

import com.biz.ontology.api.common.PageResponse;
import com.biz.ontology.api.common.R;
import com.biz.ontology.api.ontology.dto.CreateOntologyRequest;
import com.biz.ontology.api.ontology.dto.OntologyResponse;
import com.biz.ontology.api.ontology.dto.UpdateOntologyRequest;
import com.biz.ontology.api.ontology.dto.UpdateOntologyStatusRequest;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.ontology.service.OntologyService;
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

@Validated
@Tag(name = "本体管理", description = "本体基本信息、领域关联和状态管理")
@RestController
@RequestMapping({"/api/v1/ontologies", "/api/v1/ontology"})
public class OntologyController {

    private final OntologyService ontologyService;

    public OntologyController(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    @Operation(summary = "分页查询本体")
    @GetMapping
    public R<PageResponse<OntologyResponse>> page(
            @RequestParam(required = false) Long domainId,
            @RequestParam(defaultValue = "false") boolean unclassified,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ConfigStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return R.ok(PageResponse.from(ontologyService.page(domainId, unclassified, keyword, status, pageable)));
    }

    @Operation(summary = "创建本体及领域关联")
    @PostMapping
    public R<OntologyResponse> create(@Valid @RequestBody CreateOntologyRequest request) {
        return R.ok(ontologyService.create(request));
    }

    @Operation(summary = "查询本体详情")
    @GetMapping("/{id}")
    public R<OntologyResponse> getById(@PathVariable Long id) {
        return R.ok(ontologyService.getById(id));
    }

    @Operation(summary = "编辑本体及领域关联")
    @PutMapping("/{id}")
    public R<OntologyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOntologyRequest request) {
        return R.ok(ontologyService.update(id, request));
    }

    @Operation(summary = "启用或禁用本体")
    @PatchMapping("/{id}/status")
    public R<OntologyResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOntologyStatusRequest request) {
        return R.ok(ontologyService.updateStatus(id, request.getStatus(), request.getVersion()));
    }

    @Operation(summary = "删除未被引用的本体")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        ontologyService.delete(id);
        return R.ok();
    }

    @GetMapping("/ping")
    public R<Map<String, String>> ping() {
        return R.ok(Map.of("module", "ontology", "status", "ready"));
    }
}
