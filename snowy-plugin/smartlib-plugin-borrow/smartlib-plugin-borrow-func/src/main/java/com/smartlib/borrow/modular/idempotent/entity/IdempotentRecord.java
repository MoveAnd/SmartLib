package com.smartlib.borrow.modular.idempotent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

import java.util.Date;

/** 通用幂等记录。 */
@Getter
@Setter
@TableName(value = "idempotent_record", autoResultMap = true)
public class IdempotentRecord extends CommonEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /** 操作类型 */
    @TableField("operation")
    private String operation;

    /** 调用方范围，如 C:用户ID、B:用户ID、SERVICE:服务标识 */
    @TableField("caller_scope")
    private String callerScope;

    /** 幂等键：请求号或内部业务标识 */
    @TableField("idempotency_key")
    private String idempotencyKey;

    /** 规范化业务参数的 SHA-256 摘要 */
    @TableField("request_hash")
    private String requestHash;

    /** 处理状态，对应 IdempotentStatusEnum 的值 */
    @TableField("status")
    private String status;

    /** 响应结果，保存 code、msg、data */
    @TableField(value = "result_json", typeHandler = JacksonTypeHandler.class)
    private JsonNode resultJson;

    /** 当前执行关联的 Seata 全局事务 ID */
    @TableField("global_xid")
    private String globalXid;

    /** 处理超时核查时间，超时不代表可以直接重试 */
    @TableField("processing_deadline")
    private Date processingDeadline;
}
