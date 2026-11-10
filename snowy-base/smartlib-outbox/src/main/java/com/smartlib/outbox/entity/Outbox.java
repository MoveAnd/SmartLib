package com.smartlib.outbox.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartlib.outbox.enums.OutboxStatus;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

import java.time.LocalDateTime;

/**
 * 业务库中的事务出站事件。
 */
@Getter
@Setter
@TableName(value = "outbox", autoResultMap = true)
public class Outbox extends CommonEntity {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 事件唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 聚合类型
     */
    private String aggregateType;

    /**
     * 聚合标识
     */
    private String aggregateId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件内容
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private JsonNode payload;

    /**
     * 投递状态（PENDING：待投递，SENT：已投递）
     */
    private OutboxStatus status;

    /**
     * 累计投递失败次数
     */
    private Long retryCount;

    /**
     * 下次可投递时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 最近一次投递失败原因
     */
    private String lastError;

    /**
     * 消息投递确认时间
     */
    private LocalDateTime sentTime;
}
