package com.smartlib.book.core.config;

import com.smartlib.book.core.consts.IdempotentConstant;
import com.smartlib.idempotent.service.IdempotentOperationPolicy;
import com.smartlib.idempotent.support.IdempotentCallerResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vip.xiaonuo.auth.core.util.CallerScopeUtil;

import java.util.Map;
import java.util.function.Supplier;

/** 为公共幂等组件声明 Book 服务允许的操作。 */
@Configuration(proxyBeanMethods = false)
public class BookIdempotencyConfiguration {

    /** 幂等操作与调用者身份范围解析规则的映射。 */
    private static final Map<String, Supplier<String>> CALLER_SCOPE_RESOLVERS = Map.of(
            IdempotentConstant.BORROW_STOCK, CallerScopeUtil::currentReaderScope,
            IdempotentConstant.RETURN_STOCK, CallerScopeUtil::currentAdminScope
    );

    @Bean
    IdempotentOperationPolicy bookIdempotentOperations() {
        return CALLER_SCOPE_RESOLVERS::containsKey;
    }

    @Bean
    IdempotentCallerResolver bookIdempotentCallerResolver() {
        return IdempotentCallerResolver.fromOperations(CALLER_SCOPE_RESOLVERS);
    }
}
