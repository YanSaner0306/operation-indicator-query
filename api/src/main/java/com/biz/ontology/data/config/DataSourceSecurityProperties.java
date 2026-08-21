/**
 * 模块6-8：外部数据源安全配置。
 * 功能：集中承载加密密钥、地址白名单、超时和连接池上限等可外部化参数。
 * 技术栈：Spring Boot ConfigurationProperties。
 */
package com.biz.ontology.data.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "platform.data-source")
public class DataSourceSecurityProperties {
    private String encryptionKey;
    private String keyVersion = "v1";
    private List<String> allowedHosts = new ArrayList<>();
    private boolean allowPrivateAddresses;
    private int connectTimeoutMs = 5000;
    private int queryTimeoutSeconds = 10;
    private int maxPoolSize = 5;
    public String getEncryptionKey() { return encryptionKey; }
    public void setEncryptionKey(String value) { encryptionKey = value; }
    public String getKeyVersion() { return keyVersion; }
    public void setKeyVersion(String value) { keyVersion = value; }
    public List<String> getAllowedHosts() { return allowedHosts; }
    public void setAllowedHosts(List<String> value) { allowedHosts = value; }
    public boolean isAllowPrivateAddresses() { return allowPrivateAddresses; }
    public void setAllowPrivateAddresses(boolean value) { allowPrivateAddresses = value; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int value) { connectTimeoutMs = value; }
    public int getQueryTimeoutSeconds() { return queryTimeoutSeconds; }
    public void setQueryTimeoutSeconds(int value) { queryTimeoutSeconds = value; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(int value) { maxPoolSize = value; }
}
