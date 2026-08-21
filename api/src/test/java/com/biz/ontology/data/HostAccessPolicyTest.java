/**
 * 模块7测试：验证SSRF地址白名单和私网默认拒绝策略。
 * 技术栈：JUnit 5、AssertJ、InetAddress与配置属性对象。
 */
package com.biz.ontology.data;

import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.data.config.DataSourceSecurityProperties;
import com.biz.ontology.data.security.HostAccessPolicy;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class HostAccessPolicyTest {
    @Test void shouldRejectLoopbackByDefaultAndAllowExplicitDevelopmentException() {
        DataSourceSecurityProperties properties = new DataSourceSecurityProperties(); properties.setAllowedHosts(List.of("localhost"));
        HostAccessPolicy policy = new HostAccessPolicy(properties);
        assertThatThrownBy(() -> policy.validate("localhost")).isInstanceOf(BusinessException.class);
        properties.setAllowPrivateAddresses(true);
        assertThat(policy.validate("localhost")).isNotEmpty();
    }

    @Test void shouldRejectHostOutsideAllowlist() {
        DataSourceSecurityProperties properties = new DataSourceSecurityProperties(); properties.setAllowedHosts(List.of("192.0.2.0/24"));
        assertThatThrownBy(() -> new HostAccessPolicy(properties).validate("localhost")).isInstanceOf(BusinessException.class);
    }
}
