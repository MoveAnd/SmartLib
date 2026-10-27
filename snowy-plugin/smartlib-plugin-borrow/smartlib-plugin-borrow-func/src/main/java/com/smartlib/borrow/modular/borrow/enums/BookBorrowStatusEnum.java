package com.smartlib.borrow.modular.borrow.enums;

import lombok.Getter;
import vip.xiaonuo.common.exception.CommonException;

/** 借阅记录状态；逾期由应还时间判断，不作为独立流转状态。 */
@Getter
public enum BookBorrowStatusEnum {

    /** 借阅中 */
    BORROWING("BORROWING"),

    /** 已确认归还 */
    RETURNED("RETURNED");

    private final String value;

    BookBorrowStatusEnum(String value) {
        this.value = value;
    }

    public static void validate(String value) {
        for (BookBorrowStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return;
            }
        }
        throw new CommonException("不支持的借阅状态：{}", value);
    }
}
