package com.biz.ontology.api.data;

import com.biz.ontology.api.common.R;
import com.biz.ontology.api.data.dto.MappedRecordResponse;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.data.query.MappedDataQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/mapped-data/{ontologyId}/records")
@PreAuthorize("hasAuthority('ONTOLOGY_VIEW') and hasAuthority('BINDING_VIEW')")
public class MappedDataController {
    private final MappedDataQueryService service;

    public MappedDataController(MappedDataQueryService service) { this.service = service; }

    @GetMapping("/{businessKey}")
    public R<MappedRecordResponse> get(@PathVariable Long ontologyId, @PathVariable @NotBlank String businessKey) {
        return R.ok(service.getRecord(ontologyId, businessKey).map(MappedRecordResponse::from)
                .orElseThrow(() -> new BusinessException(PlatformErrorCode.MAPPED_DATA_NOT_FOUND)));
    }

    @GetMapping
    public R<List<MappedRecordResponse>> find(@PathVariable Long ontologyId, @RequestParam @NotNull Long propertyId,
                                               @RequestParam @NotBlank String value,
                                               @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return R.ok(service.findRecords(ontologyId, propertyId, value, limit).stream().map(MappedRecordResponse::from).toList());
    }
}
