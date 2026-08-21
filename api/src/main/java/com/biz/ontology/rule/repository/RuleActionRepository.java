package com.biz.ontology.rule.repository;

import com.biz.ontology.rule.model.RuleActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RuleActionRepository extends JpaRepository<RuleActionEntity, Long> {
    Optional<RuleActionEntity> findByRuleVersionId(Long ruleVersionId);
}
