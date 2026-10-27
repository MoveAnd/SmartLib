package com.smartlib.idempotent.support;

import com.smartlib.idempotent.entity.IdempotentRecord;
import com.smartlib.idempotent.service.IdempotentRecordService;
import com.smartlib.seata.client.SeataGlobalTransactionClient;
import org.apache.seata.core.model.GlobalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;

/** 定时补查已过核查时间的未决记录；无执行权接管，不对 TC 发起提交/回滚。 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnExpression("${smartlib.idempotent.reconciliation.enabled:${borrow.idempotent.reconciliation.enabled:true}}")
public class IdempotentReconciliationJob {

    private static final Logger LOG = LoggerFactory.getLogger(IdempotentReconciliationJob.class);

    private final IdempotentRecordService records;
    private final SeataGlobalTransactionClient seata;
    private final int batchSize;
    private final int maxBatches;
    private String cursor;

    public IdempotentReconciliationJob(IdempotentRecordService records, SeataGlobalTransactionClient seata,
            @Value("${smartlib.idempotent.reconciliation.batch-size:${borrow.idempotent.reconciliation.batch-size:100}}") int batchSize,
            @Value("${smartlib.idempotent.reconciliation.max-batches:${borrow.idempotent.reconciliation.max-batches:10}}") int maxBatches) {
        if (batchSize < 1 || batchSize > 1000 || maxBatches < 1) {
            throw new IllegalArgumentException("幂等核查每批数量须为1到1000，最大批数须大于0");
        }
        this.records = records;
        this.seata = seata;
        this.batchSize = batchSize;
        this.maxBatches = maxBatches;
    }

    @Scheduled(initialDelayString = "${smartlib.idempotent.reconciliation.initial-delay-ms:${borrow.idempotent.reconciliation.initial-delay-ms:60000}}",
            fixedDelayString = "${smartlib.idempotent.reconciliation.interval-ms:${borrow.idempotent.reconciliation.interval-ms:60000}}")
    public synchronized void reconcile() {
        Date checkedAt = new Date();
        try {
            for (int batch = 0; batch < maxBatches; batch++) {
                List<IdempotentRecord> pending = records.findPending(checkedAt, cursor, batchSize);
                if (pending.isEmpty()) {
                    cursor = null;
                    return;
                }
                for (IdempotentRecord record : pending) {
                    reconcile(record);
                    cursor = record.getId();
                }
                if (pending.size() < batchSize) {
                    cursor = null;
                    return;
                }
            }
            // 保留游标，下一轮从后续记录继续，避免较早的长期未决记录饿死其他记录。
        } catch (Exception failure) {
            LOG.warn("IDEMPOTENT_RECONCILIATION_FAILED cursor={} reason=scan-failed", cursor, failure);
        }
    }

    private void reconcile(IdempotentRecord record) {
        String xid = record.getGlobalXid();
        if (xid == null || xid.isBlank()) {
            LOG.warn("IDEMPOTENT_UNRESOLVED recordId={} xid={} status={} reason=missing-xid manualCheck=true",
                    record.getId(), xid, record.getStatus());
            return;
        }
        try {
            GlobalStatus outcome = seata.getStatus(xid);
            if (!records.confirmGlobalResult(record.getId(), xid, outcome)) {
                LOG.warn("IDEMPOTENT_UNRESOLVED recordId={} xid={} status={} globalStatus={} manualCheck=true",
                        record.getId(), xid, record.getStatus(), outcome);
            }
        } catch (Exception failure) {
            LOG.warn("IDEMPOTENT_UNRESOLVED recordId={} xid={} status={} reason=query-or-write-failed manualCheck=true",
                    record.getId(), xid, record.getStatus(), failure);
        }
    }
}
