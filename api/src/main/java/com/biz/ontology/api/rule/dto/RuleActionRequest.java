package com.biz.ontology.api.rule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleActionRequest {
    @NotBlank(message = "结果编码不能为空")
    @Size(max = 100, message = "结果编码长度不能超过100个字符")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "结果编码只能包含字母、数字和下划线，且必须以字母开头")
    private String resultCode;

    @NotBlank(message = "结果名称不能为空")
    @Size(max = 100, message = "结果名称长度不能超过100个字符")
    private String resultName;

    @Size(max = 1000, message = "提示信息长度不能超过1000个字符")
    private String message;
}
