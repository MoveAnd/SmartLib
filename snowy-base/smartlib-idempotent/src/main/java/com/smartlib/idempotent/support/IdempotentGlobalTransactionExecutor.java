package com.smartlib.idempotent.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartlib.idempotent.service.IdempotentRecordService;
import com.smartlib.seata.transaction.SeataTransactionCallback;
import com.smartlib.seata.transaction.SeataTransactionException;
import com.smartlib.seata.transaction.SeataTransactionExecutor;
import org.apache.seata.core.model.GlobalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vip.xiaonuo.common.exception.CommonException;

import java.util.function.Supplier;

/** 将幂等状态转换适配到通用事务回调，事务生命周期由 smartlib-seata 管理。 */
public class IdempotentGlobalTransactionExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(IdempotentGlobalTransactionExecutor.class);
    private final IdempotentRecordService records;
    private final SeataTransactionExecutor transactions;

    public IdempotentGlobalTransactionExecutor(IdempotentRecordService records, SeataTransactionExecutor transactions) {
        this.records = records;
        this.transactions = transactions;
    }

    public JsonNode execute(String recordId, Supplier<JsonNode> business) {
        return execute(recordId, "idempotent", 60000, business);
    }

    public JsonNode execute(String recordId, String transactionName, int timeoutMillis, Supplier<JsonNode> business) {
        requireCallback(recordId, business);
        try {
            return transactions.execute(transactionName, timeoutMillis, callback(recordId, business));
        } catch (SeataTransactionException failure) {
            throw businessException(failure);
        }
    }

    public JsonNode executeParticipant(String recordId, int timeoutMillis, Supplier<JsonNode> business) {
        requireCallback(recordId, business);
        try {
            return transactions.executeParticipant(timeoutMillis, callback(recordId, business));
        } catch (SeataTransactionException failure) {
            throw businessException(failure);
        }
    }

    private SeataTransactionCallback<JsonNode> callback(String recordId, Supplier<JsonNode> business) {
        return new SeataTransactionCallback<>() {
            @Override public void beforeLocalTransaction(String xid) {
                records.bindGlobalTransaction(recordId, xid);
            }
            @Override public JsonNode executeLocal(String xid) {
                return IdempotentResponseSnapshot.copyOf(business.get());
            }
            @Override public void afterLocalCommit(String xid, JsonNode response) {
                records.markExecuted(recordId, xid, response);
            }
            @Override public void afterCompletion(String xid, GlobalStatus outcome) {
                try {
                    if (!records.confirmGlobalResult(recordId, xid, outcome)) {
                        LOG.warn("IDEMPOTENT_UNRESOLVED recordId={} xid={} globalStatus={} reason=not-transitioned manualCheck=true",
                                recordId, xid, outcome);
                    }
                } catch (Exception failure) {
                    LOG.warn("IDEMPOTENT_UNRESOLVED recordId={} xid={} reason=outcome-write-failed manualCheck=true",
                            recordId, xid, failure);
                }
            }
        };
    }

    private void requireCallback(String recordId, Supplier<JsonNode> business) {
        if (recordId == null || recordId.isBlank() || business == null) {
            throw new IllegalArgumentException("幂等记录ID与业务回调不能为空");
        }
    }

    private CommonException businessException(SeataTransactionException failure) {
        CommonException exception = new CommonException(failure.getMessage());
        exception.initCause(failure);
        return exception;
    }
}
