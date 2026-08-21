/**
 * 模块4：Refresh Token数据访问接口。
 * 功能：按摘要定位令牌，并支持按轮换族批量吊销。
 * 技术栈：Spring Data JPA派生查询与批量更新。
 */
package com.biz.ontology.auth.token.repository;

import com.biz.ontology.auth.token.model.RefreshTokenEntity;
import com.biz.ontology.auth.token.model.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshTokenEntity token set token.status = :status, token.revokedAt = :revokedAt " +
            "where token.familyId = :familyId and token.status = com.biz.ontology.auth.token.model.TokenStatus.ACTIVE")
    int revokeActiveFamily(
            @Param("familyId") String familyId,
            @Param("status") TokenStatus status,
            @Param("revokedAt") LocalDateTime revokedAt
    );
}
