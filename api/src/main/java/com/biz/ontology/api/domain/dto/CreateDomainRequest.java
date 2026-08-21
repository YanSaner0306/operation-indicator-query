package com.biz.ontology.api.domain.dto;

import com.biz.ontology.domain.enums.DomainStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateDomainRequest {

    private Long parentId;

    @NotBlank(message = "领域名称不能为空")
    @Size(max = 64, message = "领域名称长度不能超过64个字符")
    private String name;

    @NotBlank(message = "领域编码不能为空")
    @Size(max = 64, message = "领域编码长度不能超过64个字符")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "领域编码只能包含字母、数字和下划线，且必须以字母开头")
    private String code;

    @Size(max = 500, message = "领域说明长度不能超过500个字符")
    private String description;

    private DomainStatus status = DomainStatus.ENABLED;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortOrder = 0;

    public CreateDomainRequest() {
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DomainStatus getStatus() {
        return status;
    }

    public void setStatus(DomainStatus status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
