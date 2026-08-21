package com.biz.ontology.api.rule;

import com.biz.ontology.api.common.R;
import com.biz.ontology.api.rule.dto.RuleTestRequest;
import com.biz.ontology.api.rule.dto.RuleTestResponse;
import com.biz.ontology.rule.service.RuleTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "规则测试", description = "不访问真实业务数据库的手工规则测试")
@RestController
@RequestMapping("/api/v1/rules/{ruleId}/test")
public class RuleTestController {

    private final RuleTestService ruleTestService;

    public RuleTestController(RuleTestService ruleTestService) {
        this.ruleTestService = ruleTestService;
    }

    @Operation(summary = "手工测试指定或当前规则版本")
    @PostMapping
    public R<RuleTestResponse> test(
            @PathVariable Long ruleId,
            @Valid @RequestBody RuleTestRequest request) {
        return R.ok(ruleTestService.test(ruleId, request));
    }
}
