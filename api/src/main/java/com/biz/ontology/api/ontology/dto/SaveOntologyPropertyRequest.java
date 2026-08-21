package com.biz.ontology.api.ontology.dto;

import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.ontology.enums.PropertyDataType;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveOntologyPropertyRequest {
    @NotBlank(message = "属性名称不能为空")
    @Size(max = 64, message = "属性名称长度不能超过64个字符")
    private String name;

    @NotBlank(message = "属性编码不能为空")
    @Size(max = 64, message = "属性编码长度不能超过64个字符")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "属性编码只能包含字母、数字和下划线，且必须以字母开头")
    private String code;

    @NotNull(message = "属性数据类型不能为空")
    private PropertyDataType dataType;

    @Min(value = 1, message = "字符串长度不能小于1")
    @Max(value = 4000, message = "字符串长度不能超过4000")
    private Integer length;

    @Min(value = 1, message = "数值精度不能小于1")
    private Integer precision;

    @Min(value = 0, message = "小数位不能小于0")
    private Integer scale;

    private boolean required;

    @JsonAlias("unique")
    private boolean uniqueFlag;

    @Size(max = 500, message = "默认值长度不能超过500个字符")
    private String defaultValue;

    @Size(max = 500, message = "属性说明长度不能超过500个字符")
    private String description;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortOrder = 0;

    private ConfigStatus status = ConfigStatus.ENABLED;
}
