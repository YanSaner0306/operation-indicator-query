package com.biz.ontology.rule.service;

import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.ontology.enums.PropertyDataType;
import com.biz.ontology.rule.enums.RuleOperator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class RuleValueService {

    private static final Set<RuleOperator> NUMBER_OPERATORS = Set.of(
            RuleOperator.EQ, RuleOperator.NE, RuleOperator.GT,
            RuleOperator.GE, RuleOperator.LT, RuleOperator.LE
    );
    private static final Set<RuleOperator> STRING_OPERATORS = Set.of(
            RuleOperator.EQ, RuleOperator.NE, RuleOperator.CONTAINS,
            RuleOperator.NOT_CONTAINS, RuleOperator.IS_EMPTY, RuleOperator.IS_NOT_EMPTY
    );
    private static final Set<RuleOperator> DATE_OPERATORS = Set.of(
            RuleOperator.EQ, RuleOperator.BEFORE, RuleOperator.AFTER
    );
    private static final Set<RuleOperator> EQUALITY_OPERATORS = Set.of(RuleOperator.EQ, RuleOperator.NE);

    private final Map<PropertyDataType, Set<RuleOperator>> supportedOperators = new EnumMap<>(PropertyDataType.class);

    public RuleValueService() {
        supportedOperators.put(PropertyDataType.INTEGER, NUMBER_OPERATORS);
        supportedOperators.put(PropertyDataType.DECIMAL, NUMBER_OPERATORS);
        supportedOperators.put(PropertyDataType.STRING, STRING_OPERATORS);
        supportedOperators.put(PropertyDataType.DATE, DATE_OPERATORS);
        supportedOperators.put(PropertyDataType.DATETIME, DATE_OPERATORS);
        supportedOperators.put(PropertyDataType.BOOLEAN, EQUALITY_OPERATORS);
        supportedOperators.put(PropertyDataType.ENUM, EQUALITY_OPERATORS);
    }

    public void validateOperator(PropertyDataType dataType, RuleOperator operator) {
        if (!supportedOperators.getOrDefault(dataType, Set.of()).contains(operator)) {
            throw new BusinessException(PlatformErrorCode.RULE_OPERATOR_NOT_SUPPORTED);
        }
    }

    public String normalizeCompareValue(PropertyDataType dataType, RuleOperator operator, Object value) {
        validateOperator(dataType, operator);
        if (operator == RuleOperator.IS_EMPTY || operator == RuleOperator.IS_NOT_EMPTY) {
            return null;
        }
        if (value == null) {
            throw new BusinessException(PlatformErrorCode.RULE_COMPARE_VALUE_INVALID, "比较值不能为空");
        }
        try {
            return serialize(convert(dataType, value));
        } catch (RuntimeException exception) {
            throw new BusinessException(PlatformErrorCode.RULE_COMPARE_VALUE_INVALID);
        }
    }

    public Object normalizeActualValue(PropertyDataType dataType, RuleOperator operator, Object value) {
        if ((operator == RuleOperator.IS_EMPTY || operator == RuleOperator.IS_NOT_EMPTY) && value == null) {
            return null;
        }
        try {
            return convert(dataType, value);
        } catch (RuntimeException exception) {
            throw new BusinessException(PlatformErrorCode.RULE_VALUE_TYPE_INVALID);
        }
    }

    public boolean evaluate(
            PropertyDataType dataType,
            RuleOperator operator,
            Object actualValue,
            String expectedValue) {
        validateOperator(dataType, operator);
        if (operator == RuleOperator.IS_EMPTY) {
            return actualValue == null || actualValue.toString().isBlank();
        }
        if (operator == RuleOperator.IS_NOT_EMPTY) {
            return actualValue != null && !actualValue.toString().isBlank();
        }
        Object expected = convert(dataType, expectedValue);
        return switch (dataType) {
            case INTEGER, DECIMAL -> compareComparable((BigDecimal) actualValue, (BigDecimal) expected, operator);
            case DATE -> compareComparable((LocalDate) actualValue, (LocalDate) expected, operator);
            case DATETIME -> compareComparable((LocalDateTime) actualValue, (LocalDateTime) expected, operator);
            case BOOLEAN -> compareEquality(actualValue, expected, operator);
            case ENUM -> compareEquality(actualValue, expected, operator);
            case STRING -> compareString((String) actualValue, (String) expected, operator);
        };
    }

    private Object convert(PropertyDataType dataType, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        String text = value.toString().trim();
        return switch (dataType) {
            case INTEGER -> {
                BigDecimal integer = new BigDecimal(text).stripTrailingZeros();
                if (integer.scale() > 0) {
                    throw new IllegalArgumentException("invalid integer");
                }
                yield integer;
            }
            case DECIMAL -> new BigDecimal(text).stripTrailingZeros();
            case BOOLEAN -> parseBoolean(text);
            case DATE -> LocalDate.parse(text);
            case DATETIME -> LocalDateTime.parse(text);
            case STRING, ENUM -> value.toString();
        };
    }

    private Boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("invalid boolean");
    }

    private String serialize(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        return value.toString();
    }

    private <T extends Comparable<T>> boolean compareComparable(T actual, T expected, RuleOperator operator) {
        int result = actual.compareTo(expected);
        return switch (operator) {
            case EQ -> result == 0;
            case NE -> result != 0;
            case GT, AFTER -> result > 0;
            case GE -> result >= 0;
            case LT, BEFORE -> result < 0;
            case LE -> result <= 0;
            default -> false;
        };
    }

    private boolean compareEquality(Object actual, Object expected, RuleOperator operator) {
        boolean equal = actual.equals(expected);
        return operator == RuleOperator.EQ ? equal : !equal;
    }

    private boolean compareString(String actual, String expected, RuleOperator operator) {
        return switch (operator) {
            case EQ -> actual.equals(expected);
            case NE -> !actual.equals(expected);
            case CONTAINS -> actual.contains(expected);
            case NOT_CONTAINS -> !actual.contains(expected);
            default -> false;
        };
    }
}
