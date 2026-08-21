package com.biz.ontology.ontology.query;

public record ReferenceSummary(
        long propertyCount,
        long relationCount,
        long ruleCount,
        long bindingCount
) {
    public boolean referenced() {
        return propertyCount + relationCount + ruleCount + bindingCount > 0;
    }
}
