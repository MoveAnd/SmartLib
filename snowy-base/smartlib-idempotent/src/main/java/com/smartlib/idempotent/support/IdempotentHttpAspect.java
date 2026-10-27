package com.smartlib.idempotent.support;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.*;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.common.exception.CommonException;

import java.util.Collections;

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
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return invocation.proceed();
        }
        String key = attributes.getRequest().getHeader(KEY_HEADER);
        if (key == null) return invocation.proceed();
        if (Collections.list(attributes.getRequest().getHeaders(KEY_HEADER)).size() != 1) {
            throw new CommonException("请求只能携带一个幂等键");
        }
        String scope = callers.currentScope(); // 必须在占位和重放之前鉴权。
        JsonNode response = executor.execute(scope, operation.value(), key, invocation.getArgs(), () -> {
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
        var method = ((MethodSignature) invocation.getSignature()).getMethod();
        if (method.getReturnType() == void.class) return null;
        return json.convertValue(response, json.getTypeFactory().constructType(method.getGenericReturnType()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R rethrow(Throwable failure) throws T {
        throw (T) failure;
    }
}
