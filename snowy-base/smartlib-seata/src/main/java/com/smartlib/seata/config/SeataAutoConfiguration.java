package com.smartlib.seata.config;

import com.smartlib.seata.client.SeataGlobalTransactionClient;
import com.smartlib.seata.transaction.SeataIndependentTransactionExecutor;
import com.smartlib.seata.transaction.SeataTransactionExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

@AutoConfiguration(after = DataSourceTransactionManagerAutoConfiguration.class)
public class SeataAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    SeataGlobalTransactionClient seataGlobalTransactionClient() {
        return new SeataGlobalTransactionClient();
    }

    @Bean
    @ConditionalOnBean(PlatformTransactionManager.class)
    @ConditionalOnMissingBean
    SeataTransactionExecutor seataTransactionExecutor(SeataGlobalTransactionClient seata,
            PlatformTransactionManager transactions) {
        return new SeataTransactionExecutor(seata, transactions);
    }

    @Bean
    @ConditionalOnBean(PlatformTransactionManager.class)
    @ConditionalOnMissingBean
    SeataIndependentTransactionExecutor seataIndependentTransactionExecutor(PlatformTransactionManager transactions) {
        return new SeataIndependentTransactionExecutor(transactions);
    }
}
