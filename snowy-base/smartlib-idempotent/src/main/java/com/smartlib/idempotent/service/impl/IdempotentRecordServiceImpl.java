package com.smartlib.idempotent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartlib.idempotent.service.IdempotentOperationPolicy;
import com.smartlib.idempotent.entity.IdempotentRecord;
import com.smartlib.idempotent.enums.IdempotentStatusEnum;
import com.smartlib.idempotent.mapper.IdempotentRecordMapper;
import com.smartlib.idempotent.result.IdempotentAcquireResult;
import com.smartlib.idempotent.service.IdempotentRecordService;
import com.smartlib.idempotent.support.IdempotentResponseSnapshot;
import com.smartlib.seata.transaction.SeataIndependentTransactionExecutor;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.core.model.GlobalStatus;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import vip.xiaonuo.common.enums.CommonExceptionEnum;
import vip.xiaonuo.common.exception.CommonException;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * 幂等占位使用唯一约束兜底，重复键异常在事务结束后处理。
 */
public class IdempotentRecordServiceImpl implements IdempotentRecordService {

    private final IdempotentRecordMapper mapper;
    private final SeataIndependentTransactionExecutor independentTransactions;
    private final IdempotentOperationPolicy operationPolicy;

    public IdempotentRecordServiceImpl(IdempotentRecordMapper mapper,
                                       SeataIndependentTransactionExecutor independentTransactions, IdempotentOperationPolicy operationPolicy) {
        this.mapper = mapper;
        this.operationPolicy = operationPolicy;
        this.independentTransactions = independentTransactions;
    }

    @Override
    public IdempotentAcquireResult acquire(String callerScope, String operation, String idempotencyKey,
                                           String requestHash, Date processingDeadline) {
        if (operation == null || !operationPolicy.allows(operation)) {
            throw new CommonException(CommonExceptionEnum.ERROR415.getCode(), "不支持的幂等操作类型");
        }
        if (callerScope == null || !callerScope.matches("[\\x21-\\x7e]{1,64}")
                || idempotencyKey == null || !idempotencyKey.matches("[\\x21-\\x7e]{1,64}")) {
            throw new CommonException(CommonExceptionEnum.ERROR415.getCode(), "调用范围或幂等键格式不正确");
        }
        IdempotentRecord existing = findByKeyInNewTransaction(callerScope, operation, idempotencyKey);
        if (existing != null) {
            return handleExistingRecord(existing, requestHash);
        }
        if (processingDeadline == null || processingDeadline.getTime() <= System.currentTimeMillis()) {
            throw new CommonException(CommonExceptionEnum.ERROR415.getCode(), "处理核查时间必须晚于当前时间");
        }

        IdempotentRecord record = new IdempotentRecord();
        record.setCallerScope(callerScope);
        record.setOperation(operation);
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(requestHash);
        record.setStatus(IdempotentStatusEnum.PROCESSING.getValue());
        record.setProcessingDeadline(new Date(processingDeadline.getTime()));
        try {
            independently(() -> mapper.insert(record));
        } catch (DuplicateKeyException e) {
            // 竞争请求可能在首次查询后提交；在新事务中读取，避免复用旧快照。
            existing = findByKeyInNewTransaction(callerScope, operation, idempotencyKey);
            if (existing == null) {
                throw e;
            }
            return handleExistingRecord(existing, requestHash);
        }
        return new IdempotentAcquireResult(true, record.getId(), IdempotentStatusEnum.PROCESSING, null);
    }

    private IdempotentRecord findByKeyInNewTransaction(String callerScope, String operation, String idempotencyKey) {
        return independently(() -> mapper.selectOne(new LambdaQueryWrapper<IdempotentRecord>()
                .eq(IdempotentRecord::getCallerScope, callerScope)
                .eq(IdempotentRecord::getOperation, operation)
                .eq(IdempotentRecord::getIdempotencyKey, idempotencyKey)));
    }

    @Override
    public void bindGlobalTransaction(String recordId, String xid) {
        requireIdentity(recordId, xid);
        if (!xid.equals(RootContext.getXID())) {
            throw new CommonException("绑定的XID必须与当前全局事务一致");
        }
        independently(() -> {
            IdempotentRecord update = new IdempotentRecord();
            update.setGlobalXid(xid);
            int changed = mapper.update(update, new LambdaUpdateWrapper<IdempotentRecord>()
                    .eq(IdempotentRecord::getId, recordId)
                    .eq(IdempotentRecord::getStatus, IdempotentStatusEnum.PROCESSING.getValue())
                    .isNull(IdempotentRecord::getGlobalXid));
            if (changed != 1) {
                throw new CommonException("幂等记录已绑定事务或不允许执行");
            }
            return null;
        });
    }

    @Override
    public void markExecuted(String recordId, String xid, JsonNode response) {
        requireIdentity(recordId, xid);
        if (!xid.equals(RootContext.getXID())) {
            throw new CommonException("保存响应的XID必须与当前全局事务一致");
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new CommonException("必须在本地业务事务提交后保存幂等响应");
        }
        JsonNode snapshot = IdempotentResponseSnapshot.copyOf(response);
        independently(() -> {
            IdempotentRecord update = new IdempotentRecord();
            update.setStatus(IdempotentStatusEnum.EXECUTED.getValue());
            update.setResultJson(snapshot);
            int changed = mapper.update(update, pendingUpdate(recordId, xid)
                    .eq(IdempotentRecord::getStatus, IdempotentStatusEnum.PROCESSING.getValue()));
            if (changed != 1) {
                IdempotentRecord current = mapper.selectById(recordId);
                if (current == null || !xid.equals(current.getGlobalXid())
                        || !snapshot.equals(current.getResultJson())
                        || !(IdempotentStatusEnum.EXECUTED.getValue().equals(current.getStatus())
                        || IdempotentStatusEnum.SUCCESS.getValue().equals(current.getStatus()))) {
                    throw new CommonException("幂等记录不允许保存或覆盖响应");
                }
            }
            return null;
        });
    }

    @Override
    public boolean confirmGlobalResult(String recordId, String xid, GlobalStatus globalStatus) {
        requireIdentity(recordId, xid);
        boolean committed = globalStatus == GlobalStatus.Committed || globalStatus == GlobalStatus.AsyncCommitting;
        boolean rolledBack = globalStatus == GlobalStatus.Rollbacked || globalStatus == GlobalStatus.TimeoutRollbacked;
        if (!committed && !rolledBack) {
            return false;
        }
        return independently(() -> {
            LambdaUpdateWrapper<IdempotentRecord> condition = pendingUpdate(recordId, xid);
            if (committed) {
                IdempotentRecord record = mapper.selectById(recordId);
                if (record == null || !xid.equals(record.getGlobalXid())
                        || !IdempotentResponseSnapshot.isValid(record.getResultJson())) {
                    return false;
                }
                condition.eq(IdempotentRecord::getStatus, IdempotentStatusEnum.EXECUTED.getValue());
            } else {
                condition.in(IdempotentRecord::getStatus, IdempotentStatusEnum.PROCESSING.getValue(),
                        IdempotentStatusEnum.EXECUTED.getValue());
            }
            IdempotentRecord update = new IdempotentRecord();
            update.setStatus((committed ? IdempotentStatusEnum.SUCCESS : IdempotentStatusEnum.ROLLED_BACK).getValue());
            return mapper.update(update, condition) == 1;
        });
    }

    @Override
    public List<IdempotentRecord> findPending(Date checkedAt, String afterId, int limit) {
        if (checkedAt == null || limit < 1 || limit > 1000) {
            throw new IllegalArgumentException("核查时间不能为空，每批数量须为1到1000");
        }
        return independently(() -> mapper.selectList(new LambdaQueryWrapper<IdempotentRecord>()
                .in(IdempotentRecord::getStatus, IdempotentStatusEnum.PROCESSING.getValue(),
                        IdempotentStatusEnum.EXECUTED.getValue())
                .le(IdempotentRecord::getProcessingDeadline, checkedAt)
                .gt(afterId != null, IdempotentRecord::getId, afterId)
                .orderByAsc(IdempotentRecord::getId)
                .last("LIMIT " + limit)));
    }

    private LambdaUpdateWrapper<IdempotentRecord> pendingUpdate(String recordId, String xid) {
        return new LambdaUpdateWrapper<IdempotentRecord>()
                .eq(IdempotentRecord::getId, recordId)
                .eq(IdempotentRecord::getGlobalXid, xid);
    }

    private void requireIdentity(String recordId, String xid) {
        if (recordId == null || recordId.isBlank() || xid == null || xid.isBlank()) {
            throw new IllegalArgumentException("幂等记录ID和XID不能为空");
        }
    }

    private <T> T independently(Supplier<T> action) {
        return independentTransactions.execute(action);
    }

    private IdempotentAcquireResult handleExistingRecord(IdempotentRecord record, String requestHash) {
        if (!requestHash.equals(record.getRequestHash())) {
            throw new CommonException("幂等键已用于不同的请求参数");
        }
        IdempotentStatusEnum status;
        try {
            status = IdempotentStatusEnum.valueOf(record.getStatus());
        } catch (IllegalArgumentException e) {
            throw new CommonException("幂等记录状态异常");
        }
        if (status == IdempotentStatusEnum.SUCCESS) {
            if (!IdempotentResponseSnapshot.isValid(record.getResultJson())) {
                throw new CommonException("幂等成功记录缺少有效响应结果");
            }
            return new IdempotentAcquireResult(false, record.getId(), status, record.getResultJson().deepCopy());
        }
        // EXECUTED 不是全局成功；截止时间也不构成重新取得执行权的依据。
        return new IdempotentAcquireResult(false, record.getId(), status, null);
    }
}
