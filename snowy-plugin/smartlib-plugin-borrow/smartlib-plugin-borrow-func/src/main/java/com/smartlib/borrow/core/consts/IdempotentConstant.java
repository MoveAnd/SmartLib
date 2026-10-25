package com.smartlib.borrow.core.consts;

import java.util.Set;

/**
 * Borrow 幂等校验常量。
 */
public final class IdempotentConstant {

    /**
     * Borrow 支持的幂等操作类型
     */
    public static final Set<String> ALLOWED_OPERATIONS =
            Set.of("BORROW", "APPLY_RETURN", "CONFIRM_RETURN", "ADJUST_LIMIT");

    private IdempotentConstant() {
    }
}
