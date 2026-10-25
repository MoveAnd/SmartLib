package com.smartlib.borrow.modular.idempotent.service;

import com.smartlib.borrow.modular.idempotent.result.IdempotentAcquireResult;

import java.util.Date;

/**
 * 幂等请求占位与重复请求判断。
 */
public interface IdempotentRecordService {

    /**
     * 尝试占位；调用方须先完成鉴权，并在服务端生成调用范围和请求摘要。
     * 应在业务全局事务开启前调用，本方法只处理独立本地事务占位。
     * 已有记录返回当前状态，仅 SUCCESS 携带响应快照；超时和已回滚均不自动接管。
     *
     * @param callerScope        调用范围，如 C:用户ID、B:用户ID、SERVICE:服务标识
     * @param operation          操作类型：BORROW、APPLY_RETURN、CONFIRM_RETURN、ADJUST_LIMIT
     * @param idempotencyKey     请求号或内部业务标识
     * @param requestHash        服务端规范化业务参数的 SHA-256 十六进制摘要
     * @param processingDeadline 新请求的处理核查时间，必须晚于当前时间
     */
    IdempotentAcquireResult acquire(String callerScope, String operation, String idempotencyKey,
                                    String requestHash, Date processingDeadline);
}
