package com.biz.ontology.api.ontology;

import com.biz.ontology.api.common.R;
import com.biz.ontology.api.ontology.dto.OntologyGraphResponse;
import com.biz.ontology.ontology.service.OntologyGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "本体图谱", description = "只读本体关系图谱")
@RestController
@RequestMapping("/api/v1/ontology-graph")
public class OntologyGraphController {

    private final OntologyGraphService graphService;

    public OntologyGraphController(OntologyGraphService graphService) {
        this.graphService = graphService;
    }

    @Operation(summary = "查询只读关系图谱")
    @GetMapping
    public R<OntologyGraphResponse> graph(@RequestParam(required = false) Long domainId) {
        return R.ok(graphService.graph(domainId));
    }
}
