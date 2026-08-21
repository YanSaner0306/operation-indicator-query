package com.biz.ontology.api.rule;

import com.biz.ontology.api.common.PageResponse;
import com.biz.ontology.api.common.R;
import com.biz.ontology.api.rule.dto.RuleResponse;
import com.biz.ontology.rule.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "本体下规则", description = "本体详情页复用的规则列表入口")
@RestController
@RequestMapping("/api/v1/ontologies/{ontologyId}/rules")
public class OntologyRuleController {

    private final RuleService ruleService;

    public OntologyRuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Operation(summary = "分页查询指定本体下的规则")
    @GetMapping
    public R<PageResponse<RuleResponse>> page(
            @PathVariable Long ontologyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return R.ok(PageResponse.from(ruleService.page(keyword, ontologyId, enabled, pageable)));
    }
}
