package com.biz.ontology.api.rule;

import com.biz.ontology.api.common.R;
import com.biz.ontology.api.rule.dto.RuleResponse;
import com.biz.ontology.api.rule.dto.RuleVersionResponse;
import com.biz.ontology.api.rule.dto.SwitchRuleVersionRequest;
import com.biz.ontology.rule.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "规则版本", description = "规则历史版本查询与切换")
@RestController
@RequestMapping("/api/v1/rules/{ruleId}/versions")
public class RuleVersionController {

    private final RuleService ruleService;

    public RuleVersionController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Operation(summary = "查询规则版本列表")
    @GetMapping
    public R<List<RuleVersionResponse>> list(@PathVariable Long ruleId) {
        return R.ok(ruleService.listVersions(ruleId));
    }

    @Operation(summary = "查询规则版本详情")
    @GetMapping("/{versionId}")
    public R<RuleVersionResponse> getVersion(
            @PathVariable Long ruleId,
            @PathVariable Long versionId) {
        return R.ok(ruleService.getVersion(ruleId, versionId));
    }

    @Operation(summary = "切换当前规则版本")
    @PostMapping("/{versionId}/switch")
    public R<RuleResponse> switchVersion(
            @PathVariable Long ruleId,
            @PathVariable Long versionId,
            @RequestBody(required = false) SwitchRuleVersionRequest request) {
        Long definitionVersion = request == null ? null : request.getVersion();
        return R.ok(ruleService.switchVersion(ruleId, versionId, definitionVersion));
    }
}
