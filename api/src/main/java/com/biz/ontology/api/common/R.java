/**
 * 模块1：所有REST控制器使用的统一API响应封装。
 * 功能：承载字符串业务码、消息、数据负载和请求ID。
 * 技术栈：Java 17 + Spring MVC + Jackson；此为通用响应契约。
 * */
package com.biz.ontology.api.common;

import com.biz.ontology.common.web.RequestIdContext;

public class R<T> {
    private String code;
    private String message;
    private T data;
    private String requestId;

    public R() {
    }

    public R(String code, String message, T data, String requestId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
    }

    public static <T> R<T> ok(T data) {
        return new R<>("SUCCESS", "ok", data, RequestIdContext.current());
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> error(String code, String message) {
        return new R<>(code, message, null, RequestIdContext.current());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
