package com.smartlib.seata.config;

import feign.RequestInterceptor;
import org.apache.seata.core.context.RootContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/** Feign 为可选依赖，非 HTTP 调用方仍可单独使用事务执行器。 */
@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
public class SeataFeignAutoConfiguration {
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    @ConditionalOnMissingBean(name = "seataXidRequestInterceptor")
    RequestInterceptor seataXidRequestInterceptor() {
        return template -> {
            template.removeHeader(RootContext.KEY_XID);
            String xid = RootContext.getXID();
            if (xid != null && !xid.isBlank()) template.header(RootContext.KEY_XID, xid);
        };
    }
}
