package com.smartlib.borrow.modular.quota.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

/** 个人借阅名额。 */
@Getter
@Setter
@TableName("borrow_user_quota")
public class BorrowUserQuota extends CommonEntity {

    private static final long serialVersionUID = 1L;

    /** C端读者ID */
    @TableId(value = "user_id", type = IdType.INPUT)
    private String userId;

    /** 个人借阅上限 */
    @TableField("borrow_limit")
    private Integer borrowLimit;

    /** 未归还数量 */
    @TableField("borrowed_count")
    private Integer borrowedCount;

    /** 数据版本 */
    @TableField("row_version")
    private Long rowVersion;
}
