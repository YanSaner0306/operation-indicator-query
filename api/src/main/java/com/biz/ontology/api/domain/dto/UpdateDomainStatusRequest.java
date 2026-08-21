package com.biz.ontology.api.domain.dto;

import com.biz.ontology.domain.enums.DomainStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateDomainStatusRequest {

    @NotNull(message = "领域状态不能为空")
    private DomainStatus status;

    public UpdateDomainStatusRequest() {
    }

    public UpdateDomainStatusRequest(DomainStatus status) {
        this.status = status;
    }

    public DomainStatus getStatus() {
        return status;
    }

    public void setStatus(DomainStatus status) {
        this.status = status;
    }
}
