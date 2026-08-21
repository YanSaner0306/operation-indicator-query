/**
 * 模块1测试：验证统一响应和从1开始的分页契约。
 * 技术栈：JUnit 5 + AssertJ，不依赖Spring上下文。
 */
package com.biz.ontology.api.common;

import com.biz.ontology.common.web.RequestIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {
    @AfterEach
    void clearRequestContext() {
        RequestIdContext.clear();
    }

    @Test
    void successResponseShouldContainStringCodeAndRequestId() {
        RequestIdContext.set("req-unit-1");

        R<String> response = R.ok("payload");

        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEqualTo("payload");
        assertThat(response.getRequestId()).isEqualTo("req-unit-1");
    }

    @Test
    void pageResponseShouldExposeOneBasedPageNumber() {
        PageImpl<String> springPage = new PageImpl<>(List.of("a"), PageRequest.of(0, 20), 1);

        PageResponse<String> response = PageResponse.from(springPage);

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.items()).containsExactly("a");
    }
}
