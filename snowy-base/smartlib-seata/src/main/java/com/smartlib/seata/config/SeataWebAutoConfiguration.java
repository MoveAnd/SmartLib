package com.smartlib.seata.config;

import com.smartlib.seata.transport.SeataXidFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
@ConditionalOnClass(Filter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SeataWebAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "seataXidFilter")
    FilterRegistrationBean<SeataXidFilter> seataXidFilter() {
        FilterRegistrationBean<SeataXidFilter> registration = new FilterRegistrationBean<>(new SeataXidFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 50);
        return registration;
    }
}
