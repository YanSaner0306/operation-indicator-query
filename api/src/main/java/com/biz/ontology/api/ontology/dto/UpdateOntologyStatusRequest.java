package com.biz.ontology.api.ontology.dto;

import com.biz.ontology.common.enums.ConfigStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOntologyStatusRequest {
    @NotNull(message = "本体状态不能为空")
    private ConfigStatus status;

    private Long version;
}
