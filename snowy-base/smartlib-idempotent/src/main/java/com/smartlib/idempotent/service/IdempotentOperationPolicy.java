package com.smartlib.idempotent.service;

/** 服务自己声明允许的操作，公共组件不包含业务操作名。 */
@FunctionalInterface
public interface IdempotentOperationPolicy {
    boolean allows(String operation);
}
