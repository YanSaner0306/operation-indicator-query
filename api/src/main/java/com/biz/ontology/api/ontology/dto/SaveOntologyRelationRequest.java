package com.biz.ontology.api.ontology.dto;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.ontology.enums.RelationCardinality;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveOntologyRelationRequest {
    @NotBlank(message = "关系名称不能为空")
    @Size(max = 64, message = "关系名称长度不能超过64个字符")
    private String name;

    @NotBlank(message = "关系编码不能为空")
    @Size(max = 64, message = "关系编码长度不能超过64个字符")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "关系编码只能包含字母、数字和下划线，且必须以字母开头")
    private String code;

    @NotNull(message = "目标本体不能为空")
    private Long targetOntologyId;

    @NotNull(message = "关系基数不能为空")
    private RelationCardinality cardinality;

    private Long sourcePropertyId;
    private Long targetPropertyId;

    @Size(max = 500, message = "关系说明长度不能超过500个字符")
    private String description;

    private ConfigStatus status = ConfigStatus.ENABLED;

    private Long version;
}
