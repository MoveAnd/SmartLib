package com.smartlib.idempotent.support;

/** 从服务端认证结果生成调用范围，禁止直接相信客户端传入的用户范围。 */
@FunctionalInterface
public interface IdempotentCallerResolver {
    String currentScope();
}
