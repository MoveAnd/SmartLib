package com.smartlib.seata.transaction;

import org.apache.seata.core.context.RootContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.function.Supplier;

/** 解绑 XID 后执行独立本地事务，适用于不得随全局业务回滚的辅助状态。 */
public class SeataIndependentTransactionExecutor {
    private final TransactionTemplate transactions;

    public SeataIndependentTransactionExecutor(PlatformTransactionManager transactionManager) {
        transactions = new TransactionTemplate(transactionManager);
        transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public <T> T execute(Supplier<T> action) {
        Objects.requireNonNull(action, "独立事务回调不能为空");
        String suspendedXid = RootContext.unbind();
        try {
            return transactions.execute(status -> action.get());
        } finally {
            RootContext.unbind();
            if (suspendedXid != null) RootContext.bind(suspendedXid);
        }
    }
}
