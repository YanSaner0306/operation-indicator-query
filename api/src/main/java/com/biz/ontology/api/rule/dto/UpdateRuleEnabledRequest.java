package com.biz.ontology.api.rule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRuleEnabledRequest {
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    private Long version;
}
