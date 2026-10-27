package com.smartlib.idempotent.support;

import java.lang.annotation.*;

/** HTTP 写操作可通过 Idempotency-Key 启用幂等；原有请求体及返回类型保持不变。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotentOperation {
    String value();
}
