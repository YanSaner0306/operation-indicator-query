package com.biz.ontology.ontology.service;

import com.biz.ontology.api.ontology.dto.SaveOntologyPropertyRequest;
import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.ontology.enums.PropertyDataType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class OntologyPropertyDefinitionValidator {

    public void validate(SaveOntologyPropertyRequest request) {
        PropertyDataType type = request.getDataType();
        if (type == PropertyDataType.STRING) {
            if (request.getPrecision() != null || request.getScale() != null) {
                invalid("STRING 属性不能配置 precision/scale");
            }
        } else if (type == PropertyDataType.DECIMAL) {
            if (request.getLength() != null) {
                invalid("DECIMAL 属性不能配置 length");
            }
            if (request.getPrecision() != null
                    && request.getScale() != null
                    && request.getScale() > request.getPrecision()) {
                invalid("scale 不能大于 precision");
            }
        } else if (request.getLength() != null || request.getPrecision() != null || request.getScale() != null) {
            invalid(type + " 属性不能配置 length/precision/scale");
        }

        if (StringUtils.hasText(request.getDefaultValue())) {
            validateDefaultValue(type, request.getDefaultValue());
        }
    }

    private void validateDefaultValue(PropertyDataType type, String value) {
        try {
            switch (type) {
                case INTEGER -> new java.math.BigInteger(value.trim());
                case DECIMAL -> new BigDecimal(value.trim());
                case BOOLEAN -> {
                    if (!"true".equalsIgnoreCase(value.trim()) && !"false".equalsIgnoreCase(value.trim())) {
                        throw new IllegalArgumentException();
                    }
                }
                case DATE -> LocalDate.parse(value.trim());
                case DATETIME -> LocalDateTime.parse(value.trim());
                case STRING, ENUM -> {
                }
            }
        } catch (RuntimeException exception) {
            invalid("默认值无法按 " + type + " 解析");
        }
    }

    private void invalid(String message) {
        throw new BusinessException(PlatformErrorCode.PROPERTY_DEFINITION_INVALID, message);
    }
}
