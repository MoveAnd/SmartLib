package com.smartlib.borrow.core.config;

import com.smartlib.borrow.core.consts.IdempotentConstant;
import com.smartlib.idempotent.service.IdempotentOperationPolicy;
import org.springframework.context.annotation.*;

@Configuration(proxyBeanMethods = false)
public class BorrowIdempotencyConfiguration {
    @Bean
    IdempotentOperationPolicy borrowIdempotentOperations() {
        return IdempotentConstant.ALLOWED_OPERATIONS::contains;
    }
}
