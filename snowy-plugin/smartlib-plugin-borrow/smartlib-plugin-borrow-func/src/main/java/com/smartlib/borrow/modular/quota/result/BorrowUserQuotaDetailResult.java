package com.smartlib.borrow.modular.quota.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "本人借阅名额详情")
public class BorrowUserQuotaDetailResult {

    @Schema(description = "C端读者ID")
    private String userId;

    @Schema(description = "个人借阅上限")
    private Integer borrowLimit;

    @Schema(description = "未归还数量")
    private Integer borrowedCount;

    @Schema(description = "剩余借阅名额")
    private Integer remainingCount;
}
