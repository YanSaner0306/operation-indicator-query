/**
 * 模块7：外部数据源SSRF防护策略。
 * 功能：每次连接前重新解析主机，校验主机名/IP/CIDR白名单，并默认拒绝本机、链路本地和私网地址。
 * 技术栈：Java InetAddress、IPv4 CIDR计算与Spring配置属性。
 */
package com.biz.ontology.data.security;

import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import com.biz.ontology.data.config.DataSourceSecurityProperties;
import org.springframework.stereotype.Component;
import java.net.*;
import java.util.List;

@Component
public class HostAccessPolicy {
    private final DataSourceSecurityProperties properties;
    public HostAccessPolicy(DataSourceSecurityProperties properties) { this.properties = properties; }

    public InetAddress[] validate(String host) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            List<String> allowlist = properties.getAllowedHosts();
            if (allowlist == null || allowlist.isEmpty()) reject();
            for (InetAddress address : addresses) {
                boolean allowed = allowlist.stream().anyMatch(rule -> matches(rule.trim(), host, address));
                if (!allowed || isAlwaysForbidden(address) || (!properties.isAllowPrivateAddresses() && address.isSiteLocalAddress())) reject();
            }
            return addresses;
        } catch (UnknownHostException exception) {
            throw new BusinessException(PlatformErrorCode.DATASOURCE_HOST_FORBIDDEN, "数据源主机无法解析");
        }
    }

    private boolean isAlwaysForbidden(InetAddress address) {
        return address.isAnyLocalAddress() || address.isMulticastAddress() || address.isLinkLocalAddress()
                || (address.isLoopbackAddress() && !properties.isAllowPrivateAddresses());
    }

    private boolean matches(String rule, String host, InetAddress address) {
        if (rule.equalsIgnoreCase(host) || rule.equals(address.getHostAddress())) return true;
        if (!rule.contains("/") || !(address instanceof Inet4Address)) return false;
        try {
            String[] parts = rule.split("/", 2);
            byte[] network = InetAddress.getByName(parts[0]).getAddress();
            byte[] candidate = address.getAddress();
            int prefix = Integer.parseInt(parts[1]);
            if (network.length != 4 || prefix < 0 || prefix > 32) return false;
            int networkValue = toInt(network); int candidateValue = toInt(candidate);
            int mask = prefix == 0 ? 0 : -1 << (32 - prefix);
            return (networkValue & mask) == (candidateValue & mask);
        } catch (Exception ignored) { return false; }
    }

    private int toInt(byte[] bytes) {
        return (bytes[0] & 255) << 24 | (bytes[1] & 255) << 16 | (bytes[2] & 255) << 8 | (bytes[3] & 255);
    }

    private void reject() { throw new BusinessException(PlatformErrorCode.DATASOURCE_HOST_FORBIDDEN); }
}
