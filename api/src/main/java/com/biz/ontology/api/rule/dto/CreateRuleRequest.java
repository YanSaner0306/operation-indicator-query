package com.biz.ontology.api.rule.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRuleRequest {
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "规则编码不能为空")
    @Size(max = 100, message = "规则编码长度不能超过100个字符")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "规则编码只能包含字母、数字和下划线，且必须以字母开头")
    private String code;

    @NotNull(message = "所属本体不能为空")
    private Long ontologyId;

    @Size(max = 500, message = "规则说明长度不能超过500个字符")
    private String description;

    private boolean enabled = true;

    @Valid
    @NotNull(message = "规则条件不能为空")
    private RuleConditionRequest condition;

    @Valid
    @NotNull(message = "规则动作不能为空")
    private RuleActionRequest action;

    @Size(max = 500, message = "变更说明长度不能超过500个字符")
    private String changeNote;

    @Size(max = 100, message = "创建人长度不能超过100个字符")
    private String createdBy;
}
