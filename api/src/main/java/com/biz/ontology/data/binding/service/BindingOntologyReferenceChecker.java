/**
 * 模块12：Binding对本体模块提供的引用检查实现。
 * 功能：通过既有OntologyExternalReferenceChecker合同报告本体或属性是否仍被有效Binding引用。
 * 技术栈：Spring组件、Spring Data JPA；不让本体模块访问Binding Repository。
 */
package com.biz.ontology.data.binding.service;

import com.biz.ontology.data.binding.repository.*;
import com.biz.ontology.ontology.query.OntologyExternalReferenceChecker;
import org.springframework.stereotype.Component;

@Component
public class BindingOntologyReferenceChecker implements OntologyExternalReferenceChecker {
    private final OntologyTableBindingRepository bindings;private final OntologyFieldBindingRepository fields;
    public BindingOntologyReferenceChecker(OntologyTableBindingRepository a,OntologyFieldBindingRepository b){bindings=a;fields=b;}
    @Override public boolean hasOntologyBinding(Long ontologyId){return bindings.existsByOntologyIdAndDeletedFlagFalse(ontologyId);}
    @Override public boolean hasPropertyBinding(Long propertyId){return fields.existsActiveProperty(propertyId);}
}
