package com.smartlib.borrow.modular.borrow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

import java.util.Date;

/** 一条记录对应一位读者借出的一本馆藏。 */
@Getter
@Setter
@TableName("book_borrow")
public class BookBorrow extends CommonEntity {

    private static final long serialVersionUID = 1L;

    /** 借阅记录ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /** C端读者ID，逻辑关联读者账号 */
    @TableField("user_id")
    private String userId;

    /** 馆藏ID，逻辑关联 book_inventory.id */
    @TableField("inventory_id")
    private String inventoryId;

    /** 借阅状态，取值见 BookBorrowStatusEnum */
    @TableField("status")
    private String status;

    /** 借出时间 */
    @TableField("borrow_time")
    private Date borrowTime;

    /** 应还时间 */
    @TableField("due_time")
    private Date dueTime;

    /** 确认归还时间，尚未确认时为空 */
    @TableField("return_time")
    private Date returnTime;

    /** 实际确认归还的B端管理员ID */
    @TableField("return_user")
    private String returnUser;
}
