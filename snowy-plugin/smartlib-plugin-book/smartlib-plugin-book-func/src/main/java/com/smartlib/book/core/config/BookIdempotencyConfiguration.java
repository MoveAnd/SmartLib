package com.smartlib.book.core.config;

import com.smartlib.idempotent.service.IdempotentOperationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class BookIdempotencyConfiguration {

    @Bean
    IdempotentOperationPolicy bookIdempotentOperations() {
        return "RETURN_STOCK"::equals;
    }
}
