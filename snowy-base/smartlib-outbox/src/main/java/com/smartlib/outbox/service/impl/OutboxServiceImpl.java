package com.smartlib.outbox.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartlib.outbox.entity.Outbox;
import com.smartlib.outbox.enums.OutboxStatus;
import com.smartlib.outbox.mapper.OutboxMapper;
import com.smartlib.outbox.service.OutboxService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class OutboxServiceImpl implements OutboxService {

    /**
     * 各次投递失败后的等待时间（秒），超过列表长度后沿用最后一档。
     */
    private static final List<Integer> RETRY_DELAYS_SECONDS = List.of(5, 10, 20, 40, 80, 160, 300);

    /**
     * 写入 last_error 列的最大长度，按 Unicode 码点计数。
     */
    private static final int MAX_ERROR_LENGTH = 500;

    private final OutboxMapper mapper;

    public OutboxServiceImpl(OutboxMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public String register(String aggregateType, String aggregateId, String eventType, JsonNode payload) {
        Assert.hasText(aggregateType, "聚合类型不能为空");
        Assert.hasText(aggregateId, "聚合标识不能为空");
        Assert.hasText(eventType, "事件类型不能为空");

        Outbox event = new Outbox();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        event.setStatus(OutboxStatus.PENDING);
        event.setRetryCount(0L);
        event.setNextRetryTime(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        mapper.insert(event);
        return event.getId();
    }

    @Override
    public List<Outbox> findDue(LocalDateTime dueAt, int limit) {
        Assert.notNull(dueAt, "截止时间不能为空");
        Assert.isTrue(limit >= 1 && limit <= 1000, "每批数量须为 1 到 1000");
        return mapper.selectPage(new Page<>(1, limit, false), new LambdaQueryWrapper<Outbox>()
                .eq(Outbox::getStatus, OutboxStatus.PENDING)
                .le(Outbox::getNextRetryTime, dueAt)
                .orderByAsc(Outbox::getNextRetryTime, Outbox::getId)).getRecords();
    }

    @Override
    public boolean markSent(String eventId, LocalDateTime sentAt) {
        Assert.notNull(sentAt, "发送确认时间不能为空");
        Assert.hasText(eventId, "事件 ID 不能为空");

        LambdaUpdateWrapper<Outbox> update = new LambdaUpdateWrapper<Outbox>()
                .eq(Outbox::getId, eventId)
                .eq(Outbox::getStatus, OutboxStatus.PENDING)
                .set(Outbox::getStatus, OutboxStatus.SENT)
                .set(Outbox::getSentTime, sentAt.truncatedTo(ChronoUnit.MILLIS));
        return mapper.update(new Outbox(), update) == 1;
    }

    @Override
    public boolean markFailed(String eventId, long observedRetryCount, LocalDateTime failedAt, String lastError) {
        Assert.notNull(failedAt, "发送失败时间不能为空");
        Assert.isTrue(observedRetryCount >= 0, "失败次数不能为负数");
        Assert.hasText(eventId, "事件 ID 不能为空");

        int delayIndex = (int) Math.min(observedRetryCount, RETRY_DELAYS_SECONDS.size() - 1);
        LocalDateTime nextRetry = failedAt.truncatedTo(ChronoUnit.MILLIS)
                .plusSeconds(RETRY_DELAYS_SECONDS.get(delayIndex));
        LambdaUpdateWrapper<Outbox> update = new LambdaUpdateWrapper<Outbox>()
                .eq(Outbox::getId, eventId)
                .eq(Outbox::getStatus, OutboxStatus.PENDING)
                .eq(Outbox::getRetryCount, observedRetryCount)
                .set(Outbox::getRetryCount, observedRetryCount + 1)
                .set(Outbox::getNextRetryTime, nextRetry)
                .set(Outbox::getLastError, truncateError(lastError));
        return mapper.update(new Outbox(), update) == 1;
    }

    private String truncateError(String lastError) {
        if (lastError == null || lastError.codePointCount(0, lastError.length()) <= MAX_ERROR_LENGTH) {
            return lastError;
        }
        return lastError.substring(0, lastError.offsetByCodePoints(0, MAX_ERROR_LENGTH));
    }
}
