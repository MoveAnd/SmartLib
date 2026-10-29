package com.smartlib.borrow.core.config;

import com.smartlib.borrow.core.consts.IdempotentConstant;
import cn.dev33.satoken.stp.StpUtil;
import com.smartlib.idempotent.service.IdempotentOperationPolicy;
import com.smartlib.idempotent.support.IdempotentCallerResolver;
import org.springframework.context.annotation.*;

@Configuration(proxyBeanMethods = false)
public class BorrowIdempotencyConfiguration {
    @Bean
    IdempotentOperationPolicy borrowIdempotentOperations() {
        return IdempotentConstant.ALLOWED_OPERATIONS::contains;
    }

    @Bean
    IdempotentCallerResolver borrowIdempotentCallerResolver() {
        return () -> "B:" + StpUtil.getLoginIdAsString();
    }
}
