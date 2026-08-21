package com.biz.ontology.api.ontology;

import com.biz.ontology.api.common.R;
import com.biz.ontology.api.ontology.dto.OntologyPropertyResponse;
import com.biz.ontology.api.ontology.dto.SaveOntologyPropertyRequest;
import com.biz.ontology.ontology.service.OntologyPropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "本体属性", description = "本体属性定义与引用保护")
@RestController
@RequestMapping({
        "/api/v1/ontologies/{ontologyId}/properties",
        "/api/v1/ontology/{ontologyId}/properties"
})
public class OntologyPropertyController {

    private final OntologyPropertyService propertyService;

    public OntologyPropertyController(OntologyPropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Operation(summary = "查询本体属性")
    @GetMapping
    public R<List<OntologyPropertyResponse>> list(@PathVariable Long ontologyId) {
        return R.ok(propertyService.list(ontologyId));
    }

    @Operation(summary = "新增本体属性")
    @PostMapping
    public R<OntologyPropertyResponse> create(
            @PathVariable Long ontologyId,
            @Valid @RequestBody SaveOntologyPropertyRequest request) {
        return R.ok(propertyService.create(ontologyId, request));
    }

    @Operation(summary = "编辑本体属性")
    @PutMapping("/{propertyId}")
    public R<OntologyPropertyResponse> update(
            @PathVariable Long ontologyId,
            @PathVariable Long propertyId,
            @Valid @RequestBody SaveOntologyPropertyRequest request) {
        return R.ok(propertyService.update(ontologyId, propertyId, request));
    }

    @Operation(summary = "删除未被引用的本体属性")
    @DeleteMapping("/{propertyId}")
    public R<Void> delete(@PathVariable Long ontologyId, @PathVariable Long propertyId) {
        propertyService.delete(ontologyId, propertyId);
        return R.ok();
    }
}
