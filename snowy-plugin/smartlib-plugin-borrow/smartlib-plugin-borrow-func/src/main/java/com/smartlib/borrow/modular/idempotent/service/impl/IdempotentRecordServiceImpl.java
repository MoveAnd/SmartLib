package com.smartlib.borrow.modular.idempotent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartlib.borrow.core.consts.IdempotentConstant;
import com.smartlib.borrow.modular.idempotent.entity.IdempotentRecord;
import com.smartlib.borrow.modular.idempotent.enums.IdempotentStatusEnum;
import com.smartlib.borrow.modular.idempotent.mapper.IdempotentRecordMapper;
import com.smartlib.borrow.modular.idempotent.result.IdempotentAcquireResult;
import com.smartlib.borrow.modular.idempotent.service.IdempotentRecordService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import vip.xiaonuo.common.enums.CommonExceptionEnum;
import vip.xiaonuo.common.exception.CommonException;

import java.util.Date;
import java.util.Locale;

/**
 * 幂等占位使用唯一约束兜底，重复键异常在事务结束后处理。
 */
@Service
public class IdempotentRecordServiceImpl implements IdempotentRecordService {

    private final IdempotentRecordMapper mapper;
    private final TransactionTemplate transactionTemplate;

    public IdempotentRecordServiceImpl(IdempotentRecordMapper mapper,
                                       PlatformTransactionManager transactionManager) {
        this.mapper = mapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public IdempotentAcquireResult acquire(String callerScope, String operation, String idempotencyKey,
                                           String requestHash, Date processingDeadline) {
        if (operation == null || !IdempotentConstant.ALLOWED_OPERATIONS.contains(operation)) {
            throw new CommonException(CommonExceptionEnum.ERROR415.getCode(), "不支持的幂等操作类型");
        }
        String normalizedHash = requestHash.toLowerCase(Locale.ROOT);
        IdempotentRecord existing = findByKeyInNewTransaction(callerScope, operation, idempotencyKey);
        if (existing != null) {
            return handleExistingRecord(existing, normalizedHash);
        }
        if (processingDeadline == null || processingDeadline.getTime() <= System.currentTimeMillis()) {
            throw new CommonException(CommonExceptionEnum.ERROR415.getCode(), "处理核查时间必须晚于当前时间");
        }

        IdempotentRecord record = new IdempotentRecord();
        record.setCallerScope(callerScope);
        record.setOperation(operation);
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(normalizedHash);
        record.setStatus(IdempotentStatusEnum.PROCESSING.getValue());
        record.setProcessingDeadline(new Date(processingDeadline.getTime()));
        try {
            transactionTemplate.executeWithoutResult(status -> mapper.insert(record));
        } catch (DuplicateKeyException e) {
            // 竞争请求可能在首次查询后提交；在新事务中读取，避免复用旧快照。
            existing = findByKeyInNewTransaction(callerScope, operation, idempotencyKey);
            if (existing == null) {
                throw e;
            }
            return handleExistingRecord(existing, normalizedHash);
        }
        return new IdempotentAcquireResult(true, record.getId(), IdempotentStatusEnum.PROCESSING, null);
    }

    private IdempotentRecord findByKeyInNewTransaction(String callerScope, String operation, String idempotencyKey) {
        return transactionTemplate.execute(status -> mapper.selectOne(new LambdaQueryWrapper<IdempotentRecord>()
                .eq(IdempotentRecord::getCallerScope, callerScope)
                .eq(IdempotentRecord::getOperation, operation)
                .eq(IdempotentRecord::getIdempotencyKey, idempotencyKey)));
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
            if (record.getResultJson() == null || !record.getResultJson().isObject()) {
                throw new CommonException("幂等成功记录缺少有效响应结果");
            }
            return new IdempotentAcquireResult(false, record.getId(), status, record.getResultJson());
        }
        // EXECUTED 不是全局成功；截止时间也不构成重新取得执行权的依据。
        return new IdempotentAcquireResult(false, record.getId(), status, null);
    }
}
