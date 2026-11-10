package com.smartlib.outbox.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.smartlib.outbox.mapper.OutboxMapper;
import com.smartlib.outbox.service.OutboxService;
import com.smartlib.outbox.service.impl.OutboxServiceImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 复用应用的 MyBatis 会话和事务管理器。
 */
@AutoConfiguration(after = {MybatisPlusAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
@ConditionalOnBean({SqlSessionFactory.class, PlatformTransactionManager.class})
@MapperScan(basePackageClasses = OutboxMapper.class)
public class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OutboxService.class)
    OutboxService outboxService(OutboxMapper mapper) {
        return new OutboxServiceImpl(mapper);
    }
}
