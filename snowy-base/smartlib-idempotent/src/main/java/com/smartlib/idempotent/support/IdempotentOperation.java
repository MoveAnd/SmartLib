package com.smartlib.idempotent.support;

import java.lang.annotation.*;

/** HTTP 写操作可通过 Idempotency-Key 启用幂等；原有请求体及返回类型保持不变。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotentOperation {
    String value();

    /** 仅使用 @RequestBody 参数计算摘要；默认保留全部方法参数的摘要规则。 */
    boolean requestBodyOnly() default false;

    /** 仅使用具名 @RequestParam 参数计算摘要，排除请求头等参数。 */
    boolean requestParamsOnly() default false;

    /** 只能参与调用方的全局事务，占位和重放前均要求已有 XID。 */
    boolean participantOnly() default false;
}
