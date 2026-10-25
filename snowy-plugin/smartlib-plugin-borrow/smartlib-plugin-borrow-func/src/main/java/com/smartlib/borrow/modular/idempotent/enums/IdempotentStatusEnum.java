package com.smartlib.borrow.modular.idempotent.enums;

import lombok.Getter;

/** 幂等处理状态。 */
@Getter
public enum IdempotentStatusEnum {

    /** 处理中，执行结果尚未确定 */
    PROCESSING("PROCESSING"),

    /** 本地已执行，全局事务尚未确认成功 */
    EXECUTED("EXECUTED"),

    /** 全局事务或普通本地事务已确认提交 */
    SUCCESS("SUCCESS"),

    /** 已确认完整回滚 */
    ROLLED_BACK("ROLLED_BACK");

    private final String value;

    IdempotentStatusEnum(String value) {
        this.value = value;
    }
}
