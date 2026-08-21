package com.biz.ontology.api.rule;

import com.biz.ontology.api.common.PageResponse;
import com.biz.ontology.api.common.R;
import com.biz.ontology.api.rule.dto.CreateRuleRequest;
import com.biz.ontology.api.rule.dto.RuleResponse;
import com.biz.ontology.api.rule.dto.UpdateRuleEnabledRequest;
import com.biz.ontology.api.rule.dto.UpdateRuleRequest;
import com.biz.ontology.rule.service.RuleService;
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
@Tag(name = "规则管理", description = "单属性规则、当前版本、启停和逻辑删除")
@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Operation(summary = "分页查询规则")
    @GetMapping
    public R<PageResponse<RuleResponse>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long ontologyId,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return R.ok(PageResponse.from(ruleService.page(keyword, ontologyId, enabled, pageable)));
    }

    @Operation(summary = "新建规则并创建v1")
    @PostMapping
    public R<RuleResponse> create(@Valid @RequestBody CreateRuleRequest request) {
        return R.ok(ruleService.create(request));
    }

    @Operation(summary = "查询规则详情和当前版本")
    @GetMapping("/{id}")
    public R<RuleResponse> getById(@PathVariable Long id) {
        return R.ok(ruleService.getById(id));
    }

    @Operation(summary = "编辑规则并创建新版本")
    @PutMapping("/{id}")
    public R<RuleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRuleRequest request) {
        return R.ok(ruleService.update(id, request));
    }

    @Operation(summary = "启用或禁用规则")
    @PatchMapping("/{id}/enabled")
    public R<RuleResponse> updateEnabled(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRuleEnabledRequest request) {
        return R.ok(ruleService.updateEnabled(id, request.getEnabled(), request.getVersion()));
    }

    @Operation(summary = "逻辑删除已禁用且未引用的规则")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        ruleService.delete(id);
        return R.ok();
    }

    @GetMapping("/ping")
    public R<Map<String, String>> ping() {
        return R.ok(Map.of("module", "rule", "status", "ready"));
    }
}
