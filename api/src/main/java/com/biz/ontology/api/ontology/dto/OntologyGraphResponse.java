package com.biz.ontology.api.ontology.dto;

import java.util.List;

public record OntologyGraphResponse(
        List<Node> nodes,
        List<Edge> edges
) {
    public record Node(Long id, String name, String code) {
    }

    public record Edge(
            Long id,
            Long sourceOntologyId,
            Long targetOntologyId,
            String name,
            String code,
            String cardinality
    ) {
    }
}
