package com.smartlib.idempotent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartlib.idempotent.entity.IdempotentRecord;
import com.smartlib.idempotent.result.IdempotentAcquireResult;
import org.apache.seata.core.model.GlobalStatus;

import java.util.Date;
import java.util.List;

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
     * @param operation          操作类型，由当前服务白名单约束
     * @param idempotencyKey     请求号或内部业务标识
     * @param requestHash        服务端规范化业务参数的 SHA-256 十六进制摘要
     * @param processingDeadline 新请求的处理核查时间，必须晚于当前时间
     */
    IdempotentAcquireResult acquire(String callerScope, String operation, String idempotencyKey,
                                    String requestHash, Date processingDeadline);

    /** 业务写入前一次性绑定当前 XID；不允许重绑或重新取得执行权。 */
    void bindGlobalTransaction(String recordId, String xid);

    /** 本地业务已提交后保存完整响应；调用时不得存在未提交的 Spring 本地事务。 */
    void markExecuted(String recordId, String xid, JsonNode response);

    /**
     * 内部结果回调：仅接受 Seata 实际返回或查询到的状态，不向业务接口暴露。
     * Committed/AsyncCommitting 确认成功，Rollbacked/TimeoutRollbacked 确认回滚。
     * 返回本次是否完成状态转换；重复回调、XID 不匹配及未决状态均不会覆盖终态。
     */
    boolean confirmGlobalResult(String recordId, String xid, GlobalStatus globalStatus);

    /** 按主键游标读取超过核查时间的未决记录；截止时间不授予重新执行权。 */
    List<IdempotentRecord> findPending(Date checkedAt, String afterId, int limit);
}
