/**
 * 模块4：Access Token吊销数据访问接口。
 * 功能：按jti快速判断JWT是否已注销。
 * 技术栈：Spring Data JPA派生查询。
 */
package com.biz.ontology.auth.token.repository;

import com.biz.ontology.auth.token.model.RevokedAccessTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessTokenEntity, Long> {
    boolean existsByJti(String jti);
}
