package com.smartlib.idempotent.support;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.apache.seata.core.context.RootContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.context.request.*;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.common.exception.CommonException;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
public class IdempotentHttpAspect {
    public static final String KEY_HEADER = "Idempotency-Key";
    private final IdempotentRequestExecutor executor;
    private final IdempotentCallerResolver callers;
    private final ObjectMapper json;

    public IdempotentHttpAspect(IdempotentRequestExecutor executor, IdempotentCallerResolver callers,
            ObjectMapper json) {
        this.executor = executor;
        this.callers = callers;
        this.json = json;
    }

    @Around(value = "@annotation(operation)", argNames = "invocation,operation")
    public Object execute(ProceedingJoinPoint invocation, IdempotentOperation operation) throws Throwable {
        if (operation.participantOnly() && !RootContext.inGlobalTransaction()) {
            throw new CommonException("该操作必须参与调用方的全局事务");
        }
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return invocation.proceed();
        }
        String key = attributes.getRequest().getHeader(KEY_HEADER);
        if (key == null) return invocation.proceed();
        if (Collections.list(attributes.getRequest().getHeaders(KEY_HEADER)).size() != 1) {
            throw new CommonException("请求只能携带一个幂等键");
        }
        String scope = callers.currentScope(operation.value()); // 必须在占位和重放之前鉴权。
        Method method = AopUtils.getMostSpecificMethod(
                ((MethodSignature) invocation.getSignature()).getMethod(), invocation.getTarget().getClass());
        Object parameters = requestParameters(new HandlerMethod(invocation.getTarget(), method),
                invocation.getArgs(), operation);
        JsonNode response = executor.execute(scope, operation.value(), key, parameters, () -> {
            try {
                Object result = invocation.proceed();
                if (result == null) result = CommonResult.ok();
                if (!(result instanceof CommonResult<?> common)) {
                    throw new CommonException("幂等接口必须返回CommonResult");
                }
                ObjectNode snapshot = json.createObjectNode();
                snapshot.put("code", common.getCode());
                snapshot.put("msg", common.getMsg());
                snapshot.set("data", json.valueToTree(common.getData()));
                return snapshot;
            } catch (Throwable failure) {
                return IdempotentHttpAspect.<RuntimeException, JsonNode>rethrow(failure);
            }
        });
        if (method.getReturnType() == void.class) return null;
        return json.convertValue(response, json.getTypeFactory().constructType(method.getGenericReturnType()));
    }

    private Object requestParameters(HandlerMethod method, Object[] arguments, IdempotentOperation operation) {
        if (operation.requestBodyOnly() && operation.requestParamsOnly()) {
            throw new IllegalStateException("请求体摘要和查询参数摘要不能同时启用");
        }
        if (!operation.requestBodyOnly() && !operation.requestParamsOnly()) {
            return arguments;
        }
        // HandlerMethod 同时读取实现方法与 Feign 接口上的参数注解。
        var parameters = method.getMethodParameters();
        Map<String, Object> queryParameters = new LinkedHashMap<>();
        for (int index = 0; index < parameters.length; index++) {
            if (operation.requestBodyOnly() && parameters[index].hasParameterAnnotation(RequestBody.class)) {
                return arguments[index];
            }
            if (operation.requestParamsOnly()) {
                RequestParam requestParam = parameters[index].getParameterAnnotation(RequestParam.class);
                if (requestParam != null) {
                    String name = requestParam.name().isBlank() ? requestParam.value() : requestParam.name();
                    if (name.isBlank()) {
                        name = method.getMethod().getParameters()[index].getName();
                    }
                    queryParameters.put(name, arguments[index]);
                }
            }
        }
        if (operation.requestParamsOnly() && !queryParameters.isEmpty()) {
            return queryParameters;
        }
        throw new IllegalStateException("摘要模式要求方法声明对应的 @RequestBody 或 @RequestParam 参数");
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R rethrow(Throwable failure) throws T {
        throw (T) failure;
    }
}
