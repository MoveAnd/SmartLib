package com.smartlib.idempotent.result;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartlib.idempotent.enums.IdempotentStatusEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 幂等占位结果，仅供服务内部使用。
 */
@Getter
@RequiredArgsConstructor
public class IdempotentAcquireResult {

    /**
     * 是否取得本次执行权，只有新占位成功时为 true
     */
    private final boolean acquired;

    /**
     * 幂等记录 ID
     */
    private final String recordId;

    /**
     * 当前处理状态；已回滚记录本阶段也不自动重新占位
     */
    private final IdempotentStatusEnum status;

    /**
     * SUCCESS 时的响应快照，其他状态为空
     */
    private final JsonNode resultJson;
}
