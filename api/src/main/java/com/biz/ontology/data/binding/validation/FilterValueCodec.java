/**
 * 模块10-13：结构化筛选值类型转换器。
 * 功能：按JDBC字段类型把字符串转换为数字、布尔、日期或文本，供校验和PreparedStatement复用。
 * 技术栈：Java BigDecimal、LocalDate/LocalDateTime与ISO-8601解析。
 */
package com.biz.ontology.data.binding.validation;

import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class FilterValueCodec {
    private FilterValueCodec() {}
    public static Object parseOne(String value,String type) {
        if(value==null) return null;
        String normalized=type==null?"":type.toUpperCase(Locale.ROOT);
        try {
            if(normalized.contains("INT")) return Long.valueOf(value.trim());
            if(normalized.contains("DECIMAL")||normalized.contains("NUMERIC")||normalized.contains("DOUBLE")||normalized.contains("FLOAT")) return new BigDecimal(value.trim());
            if(normalized.equals("DATE")) return LocalDate.parse(value.trim());
            if(normalized.contains("TIME")) return LocalDateTime.parse(value.trim());
            if(normalized.contains("BOOL")||normalized.equals("BIT")||normalized.equals("TINYINT(1)")) {
                if("1".equals(value)||"true".equalsIgnoreCase(value)) return true;
                if("0".equals(value)||"false".equalsIgnoreCase(value)) return false;
                throw new IllegalArgumentException();
            }
            return value;
        } catch(Exception exception) {
            throw new BusinessException(PlatformErrorCode.BINDING_INVALID,"筛选值与字段类型不匹配");
        }
    }
    public static List<Object> parseMany(String value,String type) {
        if(value==null||value.isBlank()) throw new BusinessException(PlatformErrorCode.BINDING_INVALID,"IN条件不能为空");
        return Arrays.stream(value.split(",")).map(String::trim).filter(v->!v.isEmpty()).map(v->parseOne(v,type)).toList();
    }
    public static boolean numericOrTemporal(String type) {
        String value=type==null?"":type.toUpperCase(Locale.ROOT);
        return value.contains("INT")||value.contains("DECIMAL")||value.contains("NUMERIC")||value.contains("DOUBLE")||value.contains("FLOAT")||value.contains("DATE")||value.contains("TIME");
    }
}
