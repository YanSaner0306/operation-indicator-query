/**
 * Module 3: Permission dictionary response contract.
 * Function: exposes only stable code, display name and owning module for UI selection.
 * Stack: Java 17 record serialized by Jackson.
 */
package com.biz.ontology.api.auth.dto;

public record PermissionResponse(
        Long id,
        String code,
        String name,
        String module
) {
}
