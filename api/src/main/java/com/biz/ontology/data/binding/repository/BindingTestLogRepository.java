/**
 * 模块11：Binding测试日志仓储。
 * 功能：持久化每次测试预览的成功或失败结果。
 * 技术栈：Spring Data JPA。
 */
package com.biz.ontology.data.binding.repository;

import com.biz.ontology.data.binding.model.BindingTestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BindingTestLogRepository extends JpaRepository<BindingTestLogEntity,Long> {}
