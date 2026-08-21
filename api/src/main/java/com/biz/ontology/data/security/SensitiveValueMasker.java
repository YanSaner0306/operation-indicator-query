/**
 * 模块8与11：敏感字段识别和预览值脱敏工具。
 * 功能：统一数据源预览与Binding预览的敏感字段规则，避免不同入口泄露完整业务数据。
 * 技术栈：Java正则表达式与无状态静态工具。
 */
package com.biz.ontology.data.security;

import java.util.regex.Pattern;

public final class SensitiveValueMasker {
    private static final Pattern SENSITIVE = Pattern.compile(
            "(?i).*(password|passwd|pwd|secret|token|id[_-]?card|mobile|phone|email|身份证|手机号|手机|电话|邮箱|密码|密钥|令牌).*"
    );

    private SensitiveValueMasker() {
    }

    public static boolean isSensitive(String fieldName) {
        return fieldName != null && SENSITIVE.matcher(fieldName).matches();
    }

    public static Object mask(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value);
        return text.length() <= 4 ? "****" : text.substring(0, 2) + "****" + text.substring(text.length() - 2);
    }
}
