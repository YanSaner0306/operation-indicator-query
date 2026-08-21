/**
 * 模块1：全局REST异常转换。
 * 功能：将验证、业务、持久化和非预期失败转换为统一响应封装。
 * 技术栈：Spring Boot @RestControllerAdvice + Jackson；堆栈跟踪和SQL保留在服务端。
 */
package com.biz.ontology.api.common;

import com.biz.ontology.common.exception.BusinessException;
import com.biz.ontology.common.exception.PlatformErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

@RestControllerAdvice(basePackages = "com.biz.ontology.api")
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusinessException(BusinessException exception) {
        PlatformErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(error(errorCode, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> Objects.requireNonNullElse(error.getDefaultMessage(), "请求参数不合法"))
                .orElse("请求参数不合法");
        return ResponseEntity.badRequest().body(error(PlatformErrorCode.INVALID_REQUEST, message));
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<R<Void>> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest()
                .body(error(PlatformErrorCode.INVALID_REQUEST, PlatformErrorCode.INVALID_REQUEST.getDefaultMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<R<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException exception) {
        PlatformErrorCode code = PlatformErrorCode.OPTIMISTIC_LOCK_CONFLICT;
        return ResponseEntity.status(code.getHttpStatus()).body(error(code, code.getDefaultMessage()));
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<R<Void>> handleAccessDenied(RuntimeException exception) {
        PlatformErrorCode code = PlatformErrorCode.AUTH_PERMISSION_DENIED;
        return ResponseEntity.status(code.getHttpStatus()).body(error(code, code.getDefaultMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<R<Void>> handleDataIntegrity(DataIntegrityViolationException exception) {
        log.warn("配置数据唯一约束或外键约束冲突", exception);
        PlatformErrorCode code = PlatformErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(code.getHttpStatus()).body(error(code, "数据约束冲突"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleUnexpectedException(Exception exception) {
        log.error("本体或规则接口处理失败", exception);
        PlatformErrorCode code = PlatformErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(code.getHttpStatus()).body(error(code, code.getDefaultMessage()));
    }

    private R<Void> error(PlatformErrorCode code, String message) {
        return R.error(code.getResponseCode(), message);
    }
}
