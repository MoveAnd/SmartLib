package com.smartlib.borrow.core.consts;

/** Borrow 幂等业务操作常量。 */
public final class IdempotentConstant {

    /** 借阅。 */
    public static final String BORROW = "BORROW";

    /** 申请归还。 */
    public static final String APPLY_RETURN = "APPLY_RETURN";

    /** 管理员确认归还。 */
    public static final String CONFIRM_RETURN = "CONFIRM_RETURN";

    private IdempotentConstant() {
    }
}
