package com.smartlib.outbox.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartlib.outbox.entity.Outbox;

import java.time.LocalDateTime;
import java.util.List;

/** 出站事件持久化接口。由应用负责发送。 */
public interface OutboxService {

    /** 在当前业务事务内登记立即可投递的事件，返回事件 ID。payload 可为空。 */
    String register(String aggregateType, String aggregateId, String eventType, JsonNode payload);

    /** 按重试时间及事件 ID 升序查询到期事件，每批 1 到 1000 条。依赖应用的 MyBatis-Plus 分页插件。 */
    List<Outbox> findDue(LocalDateTime dueAt, int limit);

    /** 应用收到发送确认后调用。仅待投递事件可以转为 SENT，返回是否完成状态变更。 */
    boolean markSent(String eventId, LocalDateTime sentAt);

    /**
     * 记录发送失败并推迟下次投递。observedRetryCount 为扫描时读到的失败次数。
     * 事件已确认或失败次数已变化时返回 false。
     */
    boolean markFailed(String eventId, long observedRetryCount, LocalDateTime failedAt, String lastError);
}
