/**
 * 模块14：API客户端仓储。
 * 功能：提供软删除详情、clientId唯一性和分页查询。
 * 技术栈：Spring Data JPA与Specification。
 */
package com.biz.ontology.auth.apiclient.repository;
import com.biz.ontology.auth.apiclient.model.ApiClientEntity;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
public interface ApiClientRepository extends JpaRepository<ApiClientEntity,Long>,JpaSpecificationExecutor<ApiClientEntity>{Optional<ApiClientEntity> findByIdAndDeletedFlagFalse(Long id);boolean existsByClientIdAndDeletedFlagFalse(String clientId);boolean existsByClientIdAndIdNotAndDeletedFlagFalse(String clientId,Long id);}
