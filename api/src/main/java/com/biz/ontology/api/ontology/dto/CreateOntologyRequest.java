package com.biz.ontology.api.ontology.dto;

import com.biz.ontology.common.enums.ConfigStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateOntologyRequest {
    @NotBlank(message = "本体名称不能为空")
    @Size(max = 64, message = "本体名称长度不能超过64个字符")
    private String name;

    @NotBlank(message = "本体编码不能为空")
    @Size(max = 64, message = "本体编码长度不能超过64个字符")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "本体编码只能包含字母、数字和下划线，且必须以字母开头")
    private String code;

    @Size(max = 500, message = "本体说明长度不能超过500个字符")
    private String description;

    private ConfigStatus status = ConfigStatus.ENABLED;

    private List<Long> domainIds = new ArrayList<>();
}
