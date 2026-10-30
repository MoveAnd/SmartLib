package com.smartlib.book.core.consts;

/** Book 幂等业务操作常量。 */
public final class IdempotentConstant {

    /** 借出库存。 */
    public static final String BORROW_STOCK = "BORROW_STOCK";

    /** 归还库存。 */
    public static final String RETURN_STOCK = "RETURN_STOCK";

    private IdempotentConstant() {
    }
}
