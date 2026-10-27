package com.smartlib.seata.client;

import org.apache.seata.core.exception.TransactionException;
import org.apache.seata.core.model.GlobalStatus;
import org.apache.seata.tm.api.GlobalTransaction;
import org.apache.seata.tm.api.GlobalTransactionContext;

/** Seata 访问边界，查询只读，不对未决事务发起提交或回滚。 */
public class SeataGlobalTransactionClient {

    public GlobalTransaction create() {
        return GlobalTransactionContext.createNew();
    }

    public GlobalStatus getStatus(String xid) throws TransactionException {
        return GlobalTransactionContext.reload(xid).getStatus();
    }
}
