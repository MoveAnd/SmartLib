package com.smartlib.outbox.enums;

public enum OutboxStatus {

    /** 等待投递或重试。 */
    PENDING,

    /** 已收到发送确认。 */
    SENT
}
