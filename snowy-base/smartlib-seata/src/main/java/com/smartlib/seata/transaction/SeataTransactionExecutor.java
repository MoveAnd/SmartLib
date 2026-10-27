package com.smartlib.seata.transaction;

import com.smartlib.seata.client.SeataGlobalTransactionClient;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.core.model.GlobalStatus;
import org.apache.seata.tm.api.GlobalTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/** 通用发起方/参与方执行器，不包含幂等记录、响应格式或业务操作名。 */
public class SeataTransactionExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(SeataTransactionExecutor.class);
    private final SeataGlobalTransactionClient seata;
    private final PlatformTransactionManager transactionManager;

    public SeataTransactionExecutor(SeataGlobalTransactionClient seata, PlatformTransactionManager transactionManager) {
        this.seata = seata;
        this.transactionManager = transactionManager;
    }

    public <T> T execute(String transactionName, int timeoutMillis, SeataTransactionCallback<T> callback) {
        requireCallback(timeoutMillis, callback);
        if (transactionName == null || transactionName.isBlank()) throw new IllegalArgumentException("事务名不能为空");
        if (RootContext.inGlobalTransaction()) throw new SeataTransactionException("全局事务发起入口不能嵌套全局事务");
        GlobalTransaction transaction = seata.create();
        try {
            try {
                transaction.begin(timeoutMillis, transactionName);
            } catch (Exception failure) {
                LOG.warn("SEATA_TRANSACTION_UNRESOLVED xid={} reason=begin-failed", transaction.getXid(), failure);
                throw propagate(failure);
            }
            long startedAt = System.nanoTime();
            T result;
            try {
                result = executeLocal(transaction.getXid(), timeoutMillis, callback);
                if ((System.nanoTime() - startedAt) / 1000000 >= timeoutMillis) {
                    throw new SeataTransactionException("全局事务执行超时");
                }
            } catch (Throwable failure) {
                rollback(transaction, callback, failure);
                throw propagate(failure);
            }

            // 提交阶段通信失败可能已有有效提交决议，不能再请求回滚。
            try {
                transaction.commit();
            } catch (Exception failure) {
                notifyOutcome(callback, transaction.getXid(), transaction.getLocalStatus());
                LOG.warn("SEATA_TRANSACTION_UNRESOLVED xid={} reason=commit-failed", transaction.getXid(), failure);
                throw propagate(failure);
            }
            GlobalStatus outcome = transaction.getLocalStatus();
            notifyOutcome(callback, transaction.getXid(), outcome);
            if (outcome != GlobalStatus.Committed && outcome != GlobalStatus.AsyncCommitting) {
                throw new SeataTransactionException("全局事务未确认提交，请核查事务结果");
            }
            return result;
        } finally {
            if (transaction.getXid() != null && transaction.getXid().equals(RootContext.getXID())) RootContext.unbind();
        }
    }

    public <T> T executeParticipant(int timeoutMillis, SeataTransactionCallback<T> callback) {
        requireCallback(timeoutMillis, callback);
        String xid = RootContext.getXID();
        if (xid == null || xid.isBlank()) throw new SeataTransactionException("事务参与方必须携带XID");
        T result = executeLocal(xid, timeoutMillis, callback);
        GlobalStatus outcome;
        try {
            outcome = seata.getStatus(xid);
        } catch (Exception failure) {
            LOG.warn("SEATA_TRANSACTION_UNRESOLVED xid={} reason=participant-query-failed", xid, failure);
            outcome = GlobalStatus.UnKnown;
        }
        notifyOutcome(callback, xid, outcome);
        return result;
    }

    private <T> T executeLocal(String xid, int timeoutMillis, SeataTransactionCallback<T> callback) {
        callback.beforeLocalTransaction(xid);
        TransactionTemplate local = new TransactionTemplate(transactionManager);
        local.setTimeout((timeoutMillis - 1) / 1000 + 1);
        T result = local.execute(status -> callback.executeLocal(xid));
        callback.afterLocalCommit(xid, result);
        return result;
    }

    private void rollback(GlobalTransaction transaction, SeataTransactionCallback<?> callback, Throwable original) {
        try {
            transaction.rollback();
        } catch (Exception failure) {
            original.addSuppressed(failure);
            LOG.warn("SEATA_TRANSACTION_UNRESOLVED xid={} reason=rollback-failed", transaction.getXid(), failure);
        } finally {
            notifyOutcome(callback, transaction.getXid(), transaction.getLocalStatus());
        }
    }

    private void notifyOutcome(SeataTransactionCallback<?> callback, String xid, GlobalStatus outcome) {
        try {
            callback.afterCompletion(xid, outcome);
        } catch (Exception failure) {
            // 结果回写失败不覆盖业务异常或已确认的提交结果。
            LOG.warn("SEATA_OUTCOME_CALLBACK_FAILED xid={} globalStatus={}", xid, outcome, failure);
        }
    }

    private void requireCallback(int timeoutMillis, SeataTransactionCallback<?> callback) {
        if (timeoutMillis <= 0 || callback == null) throw new IllegalArgumentException("超时必须大于0，事务回调不能为空");
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new SeataTransactionException("全局事务执行入口必须位于本地事务之外");
        }
    }

    private RuntimeException propagate(Throwable failure) {
        if (failure instanceof Error error) throw error;
        if (failure instanceof RuntimeException runtime) return runtime;
        return new SeataTransactionException("全局事务通信异常，请核查事务结果", failure);
    }
}
