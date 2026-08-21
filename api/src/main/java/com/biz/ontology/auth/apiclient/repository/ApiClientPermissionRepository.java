/**
 * 模块14：API客户端权限关联仓储。
 * 功能：读取实时权限并支持事务内原子替换。
 * 技术栈：Spring Data JPA、JPQL查询和批量删除。
 */
package com.biz.ontology.auth.apiclient.repository;
import com.biz.ontology.auth.apiclient.model.ApiClientPermissionEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface ApiClientPermissionRepository extends JpaRepository<ApiClientPermissionEntity,Long>{
 @Query("select r.permissionId from ApiClientPermissionEntity r where r.apiClientId=:id") List<Long> findPermissionIds(@Param("id")Long id);
 @Modifying @Query("delete from ApiClientPermissionEntity r where r.apiClientId=:id") void deleteByApiClientId(@Param("id")Long id);
}
