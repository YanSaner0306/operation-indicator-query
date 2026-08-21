package com.biz.ontology.rule.repository;

import com.biz.ontology.rule.model.RuleVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RuleVersionRepository extends JpaRepository<RuleVersionEntity, Long> {
    List<RuleVersionEntity> findByRuleIdOrderByVersionNoDesc(Long ruleId);
    Optional<RuleVersionEntity> findByIdAndRuleId(Long id, Long ruleId);
    Optional<RuleVersionEntity> findTopByRuleIdOrderByVersionNoDesc(Long ruleId);
}
