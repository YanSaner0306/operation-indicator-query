/**
 * 模块15：审计日志查询REST接口。
 * 功能：按调用主体、动作、结果、requestId和时间范围只读分页查询审计记录。
 * 技术栈：Spring MVC、参数校验、Spring Method Security与统一分页响应。
 */
package com.biz.ontology.api.auth;
import com.biz.ontology.api.auth.dto.AuditLogResponse;import com.biz.ontology.api.common.*;import com.biz.ontology.auth.audit.model.AuditResult;import com.biz.ontology.auth.audit.service.AuditService;import jakarta.validation.constraints.*;import org.springframework.format.annotation.DateTimeFormat;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.validation.annotation.Validated;import org.springframework.web.bind.annotation.*;import java.time.LocalDateTime;
@Validated@RestController@RequestMapping("/api/v1/auth/audit-logs")@PreAuthorize("hasAuthority('AUDIT_VIEW')")
public class AuditLogController {private final AuditService service;public AuditLogController(AuditService s){service=s;}@GetMapping public R<PageResponse<AuditLogResponse>> page(@RequestParam(required=false)String requestId,@RequestParam(required=false)String principalId,@RequestParam(required=false)String action,@RequestParam(required=false)AuditResult result,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime from,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime to,@RequestParam(defaultValue="1")@Min(1)int page,@RequestParam(defaultValue="20")@Min(1)@Max(100)int size){return R.ok(service.page(requestId,principalId,action,result,from,to,page,size));}}
