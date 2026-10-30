package com.smartlib.borrow.core.config;

import com.smartlib.borrow.core.consts.IdempotentConstant;
import com.smartlib.idempotent.service.IdempotentOperationPolicy;
import com.smartlib.idempotent.support.IdempotentCallerResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vip.xiaonuo.auth.core.util.CallerScopeUtil;

import java.util.Map;
import java.util.function.Supplier;

/** 声明借阅操作与调用者身份的对应关系。 */
@Configuration(proxyBeanMethods = false)
public class BorrowIdempotencyConfiguration {

    /** 幂等操作与调用者身份范围解析规则的映射。 */
    private static final Map<String, Supplier<String>> CALLER_SCOPE_RESOLVERS = Map.of(
            IdempotentConstant.BORROW, CallerScopeUtil::currentReaderScope,
            IdempotentConstant.APPLY_RETURN, CallerScopeUtil::currentReaderScope,
            IdempotentConstant.CONFIRM_RETURN, CallerScopeUtil::currentAdminScope
    );

    @Bean
    IdempotentOperationPolicy borrowIdempotentOperations() {
        return CALLER_SCOPE_RESOLVERS::containsKey;
    }

    @Bean
    IdempotentCallerResolver borrowIdempotentCallerResolver() {
        return IdempotentCallerResolver.fromOperations(CALLER_SCOPE_RESOLVERS);
    }
}
