/**
 * 模块1：共享的基于1的分页DTO。
 * 功能：为REST契约规范化Spring Data分页结果。
 * 技术栈：Java 17 records + Spring Data JPA分页。
 */
package com.biz.ontology.api.common;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long total
) {
    public static <T> PageResponse<T> from(Page<T> result) {
        return new PageResponse<>(
                result.getContent(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements()
        );
    }
}
