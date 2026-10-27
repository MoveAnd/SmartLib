package com.smartlib.idempotent.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.smartlib.idempotent.enums.IdempotentStatusEnum;
import com.smartlib.idempotent.service.IdempotentRecordService;
import org.apache.seata.core.context.RootContext;
import vip.xiaonuo.common.exception.CommonException;

import java.security.MessageDigest;
import java.util.*;
import java.util.function.Supplier;

/** 先鉴权、服务端生成范围与摘要，再占位。重复未决请求永不进入业务回调。 */
public class IdempotentRequestExecutor {
    private final IdempotentRecordService records;
    private final IdempotentGlobalTransactionExecutor transactions;
    private final ObjectMapper json;

    public IdempotentRequestExecutor(IdempotentRecordService records,
            IdempotentGlobalTransactionExecutor transactions, ObjectMapper json) {
        this.records = records;
        this.transactions = transactions;
        this.json = json;
    }

    public JsonNode execute(String scope, String operation, String key, Object parameters,
            Supplier<JsonNode> business) {
        if (business == null || org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalArgumentException("业务回调不能为空，幂等入口须位于本地事务之外");
        }
        var acquired = records.acquire(scope, operation, key, hash(parameters),
                new Date(System.currentTimeMillis() + 60000));
        if (!acquired.isAcquired()) {
            if (acquired.getStatus() == IdempotentStatusEnum.SUCCESS) return acquired.getResultJson();
            throw new CommonException(acquired.getStatus() == IdempotentStatusEnum.ROLLED_BACK
                    ? "该请求已回滚，再次发起业务请使用新请求号" : "该请求结果尚未确认，请保留原请求号并核查处理结果");
        }
        if (RootContext.inGlobalTransaction()) {
            return transactions.executeParticipant(acquired.getRecordId(), 60000, business);
        }
        return transactions.execute(acquired.getRecordId(), operation, 60000, business);
    }

    public String hash(Object parameters) {
        try {
            byte[] bytes = json.writeValueAsBytes(canonical(json.valueToTree(parameters)));
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception failure) {
            throw new IllegalArgumentException("无法生成请求参数摘要", failure);
        }
    }

    private JsonNode canonical(JsonNode value) {
        if (value.isObject()) {
            ObjectNode object = json.createObjectNode();
            List<String> fields = new ArrayList<>();
            value.fieldNames().forEachRemaining(fields::add);
            Collections.sort(fields);
            fields.forEach(field -> object.set(field, canonical(value.get(field))));
            return object;
        }
        if (value.isArray()) {
            ArrayNode array = json.createArrayNode();
            value.forEach(item -> array.add(canonical(item)));
            return array;
        }
        return value;
    }
}
