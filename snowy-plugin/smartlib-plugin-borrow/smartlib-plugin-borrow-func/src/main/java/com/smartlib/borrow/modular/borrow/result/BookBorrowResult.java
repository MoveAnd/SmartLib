package com.smartlib.borrow.modular.borrow.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Schema(description = "借阅记录查询结果")
public class BookBorrowResult {

    @Schema(description = "借阅记录ID")
    private String id;

    @Schema(description = "C端读者ID")
    private String userId;

    @Schema(description = "馆藏ID")
    private String inventoryId;

    @Schema(description = "借阅状态：BORROWING未还、RETURNED已还")
    private String status;

    @Schema(description = "借出时间")
    private Date borrowTime;

    @Schema(description = "应还时间")
    private Date dueTime;

    @Schema(description = "确认归还时间，尚未确认时为空")
    private Date returnTime;

    @Schema(description = "实际确认归还的B端管理员ID，尚未确认时为空")
    private String returnUser;
}
