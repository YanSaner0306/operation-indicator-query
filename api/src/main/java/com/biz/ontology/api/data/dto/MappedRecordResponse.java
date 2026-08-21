package com.biz.ontology.api.data.dto;

import com.biz.ontology.data.query.MappedDataQueryService;

import java.util.Map;

public record MappedRecordResponse(Long bindingId, Long ontologyId, Object businessKey, Map<String, Object> values) {
    public static MappedRecordResponse from(MappedDataQueryService.MappedRecord record) {
        return new MappedRecordResponse(record.bindingId(), record.ontologyId(), record.businessKey(), record.values());
    }
}
