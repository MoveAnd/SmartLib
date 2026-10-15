package com.smartlib.book.core.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.stream.Collectors;

/** 将图书只读接口涉及的异常转为通用响应。 */
@Slf4j
public final class GlobalExceptionUtil {
    private GlobalExceptionUtil() {
    }

    public static CommonResult<String> getCommonResult(Exception e) {
        if (e instanceof BindException bindException) {
            String message = bindException.getBindingResult().getAllErrors().stream()
                    .map(error -> error.getDefaultMessage() == null ? "参数格式错误" : error.getDefaultMessage())
                    .collect(Collectors.joining(","));
            return CommonResult.get(415, message, null);
        }
        if (e instanceof ConstraintViolationException validationException) {
            String message = validationException.getConstraintViolations().stream()
                    .map(violation -> violation.getMessage()).collect(Collectors.joining(","));
            return CommonResult.get(415, message, null);
        }
        if (e instanceof MissingServletRequestParameterException) {
            return CommonResult.get(415, e.getMessage(), null);
        }
        if (e instanceof HttpRequestMethodNotSupportedException) {
            return CommonResult.get(405, "请求方法应为GET", null);
        }
        if (e instanceof NoResourceFoundException) {
            return CommonResult.get(404, "接口不存在", null);
        }
        if (e instanceof CommonException commonException) {
            return CommonResult.get(commonException.getCode(), commonException.getMsg(), null);
        }
        log.error(">>> 图书服务异常：", e);
        return CommonResult.error("服务器异常");
    }
}
