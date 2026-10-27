package com.smartlib.idempotent.support;

import com.fasterxml.jackson.databind.JsonNode;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.pojo.CommonResult;

/** 统一使用 CommonResult 响应，data 为 null 也是完整的成功快照。 */
public final class IdempotentResponseSnapshot {

    private IdempotentResponseSnapshot() {
    }

    public static boolean isValid(JsonNode response) {
        return response != null && response.isObject()
                && response.path("code").isIntegralNumber()
                && response.path("code").canConvertToInt()
                && response.path("code").intValue() == CommonResult.CODE_SUCCESS
                && response.path("msg").isTextual()
                && response.has("data");
    }

    public static JsonNode copyOf(JsonNode response) {
        if (!isValid(response)) {
            throw new CommonException("幂等响应必须包含成功的code、msg和data，data允许为null");
        }
        return response.deepCopy();
    }
}
