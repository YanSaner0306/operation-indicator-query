/**
 * 模块1：稳定的平台错误码字典。
 * 功能：将业务失败映射为HTTP状态和机器可读的字符串错误码。
 * 技术栈：Java 17枚举 + Spring Web HttpStatus；在所有平台模块间共享。
 */
package com.biz.ontology.common.exception;

import org.springframework.http.HttpStatus;

public enum PlatformErrorCode {
    INVALID_REQUEST(40000, HttpStatus.BAD_REQUEST, "请求参数不合法"),
    OPTIMISTIC_LOCK_CONFLICT(40900, HttpStatus.CONFLICT, "数据已被其他人修改"),

    DOMAIN_NOT_FOUND(40410, HttpStatus.NOT_FOUND, "领域不存在"),
    DOMAIN_CODE_EXISTS(40910, HttpStatus.CONFLICT, "领域编码重复"),
    DOMAIN_HAS_CHILDREN(40911, HttpStatus.CONFLICT, "领域存在子节点"),
    DOMAIN_HAS_ONTOLOGY(40912, HttpStatus.CONFLICT, "领域仍关联本体"),
    DOMAIN_PARENT_INVALID(40913, HttpStatus.CONFLICT, "父领域不合法"),
    DOMAIN_DISABLED(40914, HttpStatus.CONFLICT, "领域已禁用"),

    ONTOLOGY_NOT_FOUND(40420, HttpStatus.NOT_FOUND, "本体不存在"),
    ONTOLOGY_CODE_EXISTS(40920, HttpStatus.CONFLICT, "本体编码重复"),
    ONTOLOGY_REFERENCED(40921, HttpStatus.CONFLICT, "本体仍被引用"),
    ONTOLOGY_DISABLED(40922, HttpStatus.CONFLICT, "本体已禁用"),

    PROPERTY_NOT_FOUND(40421, HttpStatus.NOT_FOUND, "本体属性不存在"),
    PROPERTY_CODE_EXISTS(40923, HttpStatus.CONFLICT, "属性编码重复"),
    PROPERTY_UNIQUE_CONFLICT(40924, HttpStatus.CONFLICT, "同一本体已有 unique 属性"),
    PROPERTY_REFERENCED(40925, HttpStatus.CONFLICT, "属性仍被引用"),
    PROPERTY_DEFINITION_INVALID(40926, HttpStatus.CONFLICT, "属性定义不合法"),

    RELATION_NOT_FOUND(40422, HttpStatus.NOT_FOUND, "本体关系不存在"),
    RELATION_CODE_EXISTS(40927, HttpStatus.CONFLICT, "关系编码重复"),
    RELATION_PROPERTY_MISMATCH(40928, HttpStatus.CONFLICT, "关系属性不匹配"),

    RULE_NOT_FOUND(40430, HttpStatus.NOT_FOUND, "规则不存在"),
    RULE_CODE_EXISTS(40930, HttpStatus.CONFLICT, "规则编码重复"),
    RULE_ONTOLOGY_DISABLED(40931, HttpStatus.CONFLICT, "所属本体已禁用"),
    RULE_PROPERTY_NOT_FOUND(40932, HttpStatus.CONFLICT, "属性不存在或不属于所属本体"),
    RULE_OPERATOR_NOT_SUPPORTED(40933, HttpStatus.CONFLICT, "操作符不适配属性类型"),
    RULE_COMPARE_VALUE_INVALID(40934, HttpStatus.CONFLICT, "比较值格式不正确"),
    RULE_VERSION_NOT_FOUND(40431, HttpStatus.NOT_FOUND, "规则版本不存在"),
    RULE_VALUE_MISSING(40935, HttpStatus.CONFLICT, "测试时缺少属性值"),
    RULE_VALUE_TYPE_INVALID(40936, HttpStatus.CONFLICT, "测试值类型不正确"),
    RULE_DELETE_FORBIDDEN(40937, HttpStatus.CONFLICT, "规则当前不允许删除"),

    AUTH_REQUEST_INVALID(40001, HttpStatus.BAD_REQUEST, "认证请求参数不合法"),
    AUTH_MULTIPLE_CREDENTIALS(40002, HttpStatus.BAD_REQUEST, "不能同时提交两种凭证"),
    AUTH_LOGIN_FAILED(40101, HttpStatus.UNAUTHORIZED, "用户名或密码错误"),
    AUTH_TOKEN_INVALID(40102, HttpStatus.UNAUTHORIZED, "认证凭证无效"),
    AUTH_TOKEN_EXPIRED(40103, HttpStatus.UNAUTHORIZED, "认证凭证已过期"),
    AUTH_REFRESH_REPLAYED(40104, HttpStatus.UNAUTHORIZED, "刷新凭证已失效或被重复使用"),
    AUTH_API_KEY_INVALID(40105, HttpStatus.UNAUTHORIZED, "API Key无效"),
    AUTH_API_KEY_EXPIRED(40106, HttpStatus.UNAUTHORIZED, "API Key已过期"),
    AUTH_API_KEY_REVOKED(40107, HttpStatus.UNAUTHORIZED, "API Key已吊销"),
    AUTH_PRINCIPAL_DISABLED(40301, HttpStatus.FORBIDDEN, "当前主体已被禁用"),
    AUTH_PERMISSION_DENIED(40302, HttpStatus.FORBIDDEN, "当前主体缺少所需权限"),
    AUTH_USER_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "用户不存在"),
    AUTH_ROLE_NOT_FOUND(40402, HttpStatus.NOT_FOUND, "角色不存在"),
    AUTH_PERMISSION_NOT_FOUND(40403, HttpStatus.NOT_FOUND, "权限不存在"),
    AUTH_API_CLIENT_NOT_FOUND(40404, HttpStatus.NOT_FOUND, "API客户端不存在"),
    AUTH_CODE_EXISTS(40901, HttpStatus.CONFLICT, "用户名或角色编码已存在"),
    AUTH_PERMISSION_CODE_INVALID(42201, HttpStatus.UNPROCESSABLE_ENTITY, "权限码不在系统字典中"),
    AUTH_RATE_LIMITED(42901, HttpStatus.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试"),

    DATASOURCE_NOT_FOUND(40440, HttpStatus.NOT_FOUND, "数据源不存在"),
    DATASOURCE_NAME_EXISTS(40940, HttpStatus.CONFLICT, "数据源名称重复"),
    DATASOURCE_REFERENCED(40941, HttpStatus.CONFLICT, "数据源仍被引用"),
    DATASOURCE_CONFIG_INVALID(40040, HttpStatus.BAD_REQUEST, "数据源配置不合法"),
    DATASOURCE_HOST_FORBIDDEN(42240, HttpStatus.UNPROCESSABLE_ENTITY, "数据源地址不在允许范围内"),
    DATASOURCE_CONNECT_FAILED(42241, HttpStatus.UNPROCESSABLE_ENTITY, "数据源连接失败"),
    DATASOURCE_NOT_READ_ONLY(40340, HttpStatus.FORBIDDEN, "数据源账号不是只读账号"),
    DATASOURCE_METADATA_FAILED(42242, HttpStatus.UNPROCESSABLE_ENTITY, "读取数据源元数据失败"),
    DATASOURCE_TABLE_NOT_FOUND(40441, HttpStatus.NOT_FOUND, "数据表不存在"),

    BINDING_INVALID(42250, HttpStatus.UNPROCESSABLE_ENTITY, "Binding配置不合法"),
    BINDING_NOT_FOUND(40450, HttpStatus.NOT_FOUND, "Binding不存在或没有可用Binding"),
    BINDING_NAME_EXISTS(40950, HttpStatus.CONFLICT, "Binding名称重复"),
    BINDING_AMBIGUOUS(40951, HttpStatus.CONFLICT, "存在多条可用Binding候选"),
    BINDING_ENABLED_EDIT_FORBIDDEN(40952, HttpStatus.CONFLICT, "已启用Binding不能修改或删除"),
    BINDING_PREVIEW_FAILED(42251, HttpStatus.UNPROCESSABLE_ENTITY, "Binding预览失败"),
    BINDING_TEST_REQUIRED(40953, HttpStatus.CONFLICT, "Binding启用前必须测试成功"),

    INTERNAL_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "服务内部错误");

    private final int responseCode;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    PlatformErrorCode(int responseCode, HttpStatus httpStatus, String defaultMessage) {
        this.responseCode = responseCode;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public String getResponseCode() {
        return name();
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
