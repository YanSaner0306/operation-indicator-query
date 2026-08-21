/**
 * 模块1/2：请求关联上下文。
 * 功能：将当前请求ID提供给响应构建器和服务，而无需将其与HTTP耦合。
 * 技术栈：Java 17 ThreadLocal；生命周期由Spring Security请求过滤器管理。
 */
package com.biz.ontology.common.web;

public final class RequestIdContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private RequestIdContext() {
    }

    public static void set(String requestId) {
        CURRENT.set(requestId);
    }

    public static String current() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
