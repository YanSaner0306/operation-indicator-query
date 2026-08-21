/**
 * 模块15：接口限流配置。
 * 功能：集中读取普通接口和登录接口的每分钟速率与突发容量，禁止代码内散落魔法数字。
 * 技术栈：Spring Boot ConfigurationProperties。
 */
package com.biz.ontology.config;
import org.springframework.boot.context.properties.ConfigurationProperties;import org.springframework.stereotype.Component;
@Component@ConfigurationProperties(prefix="platform.traffic-control")
public class TrafficControlProperties {private int requestsPerMinute=60;private int burstCapacity=20;private int loginRequestsPerMinute=10;public int getRequestsPerMinute(){return requestsPerMinute;}public void setRequestsPerMinute(int v){requestsPerMinute=v;}public int getBurstCapacity(){return burstCapacity;}public void setBurstCapacity(int v){burstCapacity=v;}public int getLoginRequestsPerMinute(){return loginRequestsPerMinute;}public void setLoginRequestsPerMinute(int v){loginRequestsPerMinute=v;}}
