package com.biz.ontology.api.ontology;

import com.biz.ontology.api.common.R;
import com.biz.ontology.api.ontology.dto.OntologyRelationResponse;
import com.biz.ontology.api.ontology.dto.SaveOntologyRelationRequest;
import com.biz.ontology.ontology.service.OntologyRelationService;
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

@Tag(name = "本体关系", description = "本体语义关系配置")
@RestController
@RequestMapping({
        "/api/v1/ontologies/{ontologyId}/relations",
        "/api/v1/ontology/{ontologyId}/relations"
})
public class OntologyRelationController {

    private final OntologyRelationService relationService;

    public OntologyRelationController(OntologyRelationService relationService) {
        this.relationService = relationService;
    }

    @Operation(summary = "查询本体关系列表")
    @GetMapping
    public R<List<OntologyRelationResponse>> list(@PathVariable Long ontologyId) {
        return R.ok(relationService.list(ontologyId));
    }

    @Operation(summary = "新增本体关系")
    @PostMapping
    public R<OntologyRelationResponse> create(
            @PathVariable Long ontologyId,
            @Valid @RequestBody SaveOntologyRelationRequest request) {
        return R.ok(relationService.create(ontologyId, request));
    }

    @Operation(summary = "编辑本体关系")
    @PutMapping("/{relationId}")
    public R<OntologyRelationResponse> update(
            @PathVariable Long ontologyId,
            @PathVariable Long relationId,
            @Valid @RequestBody SaveOntologyRelationRequest request) {
        return R.ok(relationService.update(ontologyId, relationId, request));
    }

    @Operation(summary = "删除本体关系")
    @DeleteMapping("/{relationId}")
    public R<Void> delete(@PathVariable Long ontologyId, @PathVariable Long relationId) {
        relationService.delete(ontologyId, relationId);
        return R.ok();
    }
}
