/**
 * 模块15测试：普通接口与登录接口令牌桶限流。
 * 功能：验证同一来源在突发额度内放行、超过额度返回 429，且限流响应使用统一错误码。
 * 技术栈：JUnit 5、Spring MockHttpServletRequest/Response、Jackson 与过滤器单元测试。
 */
package com.biz.ontology.security;

import com.biz.ontology.common.security.RateLimitFilter;
import com.biz.ontology.config.TrafficControlProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {
    @Test void shouldReturn429AfterBurstCapacityIsConsumed() throws Exception {
        TrafficControlProperties p=new TrafficControlProperties();p.setRequestsPerMinute(1);p.setBurstCapacity(2);
        RateLimitFilter filter=new RateLimitFilter(p,new ObjectMapper());AtomicInteger passed=new AtomicInteger();
        for(int i=0;i<2;i++){var request=request("/api/v1/bindings");var response=new MockHttpServletResponse();filter.doFilter(request,response,(a,b)->passed.incrementAndGet());assertThat(response.getStatus()).isEqualTo(200);}
        var denied=new MockHttpServletResponse();filter.doFilter(request("/api/v1/bindings"),denied,(a,b)->passed.incrementAndGet());
        assertThat(passed).hasValue(2);assertThat(denied.getStatus()).isEqualTo(429);assertThat(denied.getContentAsString()).contains("AUTH_RATE_LIMITED");
    }
    private MockHttpServletRequest request(String uri){var request=new MockHttpServletRequest("GET",uri);request.setRemoteAddr("10.0.0.8");return request;}
}
