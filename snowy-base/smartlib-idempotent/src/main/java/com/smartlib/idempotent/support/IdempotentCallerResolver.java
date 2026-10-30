package com.smartlib.idempotent.support;

import vip.xiaonuo.common.exception.CommonException;

import java.util.Map;
import java.util.function.Supplier;

/** 根据操作选择服务端认证身份并生成调用范围，禁止相信客户端传入的用户范围。 */
@FunctionalInterface
public interface IdempotentCallerResolver {
    String currentScope(String operation);

    /** 根据服务声明的操作映射构建规则，每次调用时再解析当前身份。 */
    static IdempotentCallerResolver fromOperations(Map<String, Supplier<String>> scopeResolvers) {
        Map<String, Supplier<String>> resolvers = Map.copyOf(scopeResolvers);
        return operation -> {
            Supplier<String> resolver = operation == null ? null : resolvers.get(operation);
            if (resolver == null) {
                throw new CommonException("不支持的幂等操作类型");
            }
            return resolver.get();
        };
    }
}
