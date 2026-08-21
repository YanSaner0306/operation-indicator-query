/**
 * 模块9-13：Binding管理、校验、预览和状态REST接口。
 * 功能：提供分页CRUD、全量校验、LIMIT 1预览、启停和软删除端点，并强制BINDING权限。
 * 技术栈：Spring MVC、Bean Validation、Spring Method Security与统一响应封装。
 */
package com.biz.ontology.api.data;

import com.biz.ontology.api.common.*;
import com.biz.ontology.api.data.dto.*;
import com.biz.ontology.common.enums.ConfigStatus;
import com.biz.ontology.data.binding.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated @RestController @RequestMapping("/api/v1/bindings")
public class BindingController {
    private final BindingService service;private final BindingPreviewService previews;
    public BindingController(BindingService a,BindingPreviewService b){service=a;previews=b;}
    @GetMapping @PreAuthorize("hasAuthority('BINDING_VIEW')")
    public R<PageResponse<BindingResponse>> page(@RequestParam(required=false)String keyword,@RequestParam(required=false)Long ontologyId,@RequestParam(required=false)Long dataSourceId,@RequestParam(required=false)ConfigStatus status,@RequestParam(defaultValue="1")@Min(1)int page,@RequestParam(defaultValue="20")@Min(1)@Max(100)int size){return R.ok(service.page(keyword,ontologyId,dataSourceId,status,page,size));}
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('BINDING_VIEW')") public R<BindingResponse> get(@PathVariable Long id){return R.ok(service.get(id));}
    @PostMapping @PreAuthorize("hasAuthority('BINDING_MANAGE')") public R<BindingResponse> create(@Valid@RequestBody SaveBindingRequest request){return R.ok(service.create(request));}
    @PutMapping("/{id}") @PreAuthorize("hasAuthority('BINDING_MANAGE')") public R<BindingResponse> update(@PathVariable Long id,@Valid@RequestBody SaveBindingRequest request){return R.ok(service.update(id,request));}
    @PostMapping("/{id}/validate") @PreAuthorize("hasAuthority('BINDING_MANAGE')") public R<BindingValidationResponse> validate(@PathVariable Long id){return R.ok(service.validate(id));}
    @PostMapping("/{id}/preview") @PreAuthorize("hasAuthority('BINDING_MANAGE')") public R<BindingPreviewResponse> preview(@PathVariable Long id){return R.ok(previews.preview(id));}
    @PatchMapping("/{id}/enabled") @PreAuthorize("hasAuthority('BINDING_MANAGE')") public R<BindingResponse> status(@PathVariable Long id,@Valid@RequestBody UpdateBindingStatusRequest request){return R.ok(service.updateStatus(id,request));}
    @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('BINDING_MANAGE')") public R<Void> delete(@PathVariable Long id,@RequestParam@NotNull Long version){service.delete(id,version);return R.ok();}
}
