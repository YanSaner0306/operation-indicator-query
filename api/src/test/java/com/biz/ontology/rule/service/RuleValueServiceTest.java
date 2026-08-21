package com.biz.ontology.rule.service;

import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.ontology.enums.PropertyDataType;
import com.biz.ontology.rule.enums.RuleOperator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleValueServiceTest {

    private final RuleValueService service = new RuleValueService();

    @Test
    void numericGreaterThanShouldMatch() {
        Object actual = service.normalizeActualValue(PropertyDataType.DECIMAL, RuleOperator.GT, 300000);

        assertThat(service.evaluate(
                PropertyDataType.DECIMAL,
                RuleOperator.GT,
                actual,
                "200000"
        )).isTrue();
    }

    @Test
    void containsShouldOnlyBeAvailableForString() {
        assertThatThrownBy(() -> service.normalizeCompareValue(
                PropertyDataType.DECIMAL,
                RuleOperator.CONTAINS,
                "2"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(PlatformErrorCode.RULE_OPERATOR_NOT_SUPPORTED);
    }

    @Test
    void integerShouldRejectDecimalValue() {
        assertThatThrownBy(() -> service.normalizeActualValue(
                PropertyDataType.INTEGER,
                RuleOperator.EQ,
                new BigDecimal("1.5")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(PlatformErrorCode.RULE_VALUE_TYPE_INVALID);
    }
}
