package com.smartlib.seata.transaction;

import org.apache.seata.core.model.GlobalStatus;

/** 业务侧事务回调；基础组件不解释业务结果或持久化业务状态。 */
@FunctionalInterface
public interface SeataTransactionCallback<T> {
    /** 已取得当前 XID，尚未开启本地事务；此阶段失败由发起方请求全局回滚。 */
    default void beforeLocalTransaction(String xid) { }

    /** 在本地事务内执行业务；异常必须抛出。 */
    T executeLocal(String xid);

    /** 本地事务已经提交，全局提交尚未发起；此阶段失败仍触发发起方回滚。 */
    default void afterLocalCommit(String xid, T result) { }

    /** 仅传递实际 RPC 结果或 TC 查询结果，不将本地提交推断为全局成功。 */
    default void afterCompletion(String xid, GlobalStatus outcome) { }
}
