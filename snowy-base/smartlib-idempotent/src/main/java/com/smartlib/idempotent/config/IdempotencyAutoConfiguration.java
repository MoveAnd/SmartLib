package com.smartlib.idempotent.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.smartlib.idempotent.mapper.IdempotentRecordMapper;
import com.smartlib.idempotent.service.IdempotentOperationPolicy;
import com.smartlib.idempotent.service.IdempotentRecordService;
import com.smartlib.idempotent.service.impl.IdempotentRecordServiceImpl;
import com.smartlib.idempotent.support.*;
import com.smartlib.seata.config.SeataAutoConfiguration;
import com.smartlib.seata.transaction.SeataIndependentTransactionExecutor;
import com.smartlib.seata.transaction.SeataTransactionExecutor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;

/** 仅在服务声明操作白名单后启用持久化，Web 只读身份查询不会被要求新增幂等表。 */
@AutoConfiguration(after = {MybatisPlusAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class,
        SeataAutoConfiguration.class})
@ConditionalOnBean({IdempotentOperationPolicy.class, SqlSessionFactory.class, PlatformTransactionManager.class})
@MapperScan(basePackageClasses = IdempotentRecordMapper.class)
@Import(IdempotentReconciliationJob.class)
public class IdempotencyAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(IdempotentRecordService.class)
    IdempotentRecordService idempotentRecordService(IdempotentRecordMapper mapper,
            SeataIndependentTransactionExecutor transactions, IdempotentOperationPolicy policy) {
        return new IdempotentRecordServiceImpl(mapper, transactions, policy);
    }

    @Bean
    @ConditionalOnMissingBean
    IdempotentGlobalTransactionExecutor idempotentGlobalTransactionExecutor(IdempotentRecordService records,
            SeataTransactionExecutor transactions) {
        return new IdempotentGlobalTransactionExecutor(records, transactions);
    }

    @Bean
    @ConditionalOnMissingBean
    IdempotentRequestExecutor idempotentRequestExecutor(IdempotentRecordService records,
            IdempotentGlobalTransactionExecutor transactions, ObjectProvider<ObjectMapper> json) {
        return new IdempotentRequestExecutor(records, transactions, json.getIfAvailable(ObjectMapper::new));
    }

    @Bean
    @ConditionalOnBean(IdempotentCallerResolver.class)
    @ConditionalOnMissingBean
    IdempotentHttpAspect idempotentHttpAspect(IdempotentRequestExecutor executor,
            IdempotentCallerResolver callers, ObjectProvider<ObjectMapper> json) {
        return new IdempotentHttpAspect(executor, callers, json.getIfAvailable(ObjectMapper::new));
    }
}
