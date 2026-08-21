/**
 * 模块7：连接测试日志仓储。
 * 功能：持久化每次数据源连通性和只读权限检查结果。
 * 技术栈：Spring Data JPA。
 */
package com.biz.ontology.data.repository;

import com.biz.ontology.data.model.DataSourceTestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSourceTestLogRepository extends JpaRepository<DataSourceTestLogEntity, Long> {
}
