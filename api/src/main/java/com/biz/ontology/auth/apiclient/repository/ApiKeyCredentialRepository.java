/**
 * 模块14：API Key凭证仓储。
 * 功能：按公开keyId定位凭证并列出客户端的安全凭证摘要。
 * 技术栈：Spring Data JPA派生查询。
 */
package com.biz.ontology.auth.apiclient.repository;
import com.biz.ontology.auth.apiclient.model.ApiKeyCredentialEntity;
import com.biz.ontology.auth.token.model.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ApiKeyCredentialRepository extends JpaRepository<ApiKeyCredentialEntity,Long>{Optional<ApiKeyCredentialEntity> findByKeyId(String keyId);List<ApiKeyCredentialEntity> findByApiClientIdOrderByCreatedAtDesc(Long id);List<ApiKeyCredentialEntity> findByApiClientIdAndStatus(Long id,TokenStatus status);}
