package com.smartlib.borrow.modular.quota.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BorrowUserQuotaAdjustLimitParam extends BorrowUserQuotaDetailParam {

    @NotNull(message = "borrowLimit不能为空")
    @Min(value = 0, message = "借阅上限不能小于0")
    @Schema(description = "调整后的借阅上限，允许为0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer borrowLimit;

    @NotNull(message = "expectedVersion不能为空")
    @Min(value = 0, message = "expectedVersion不能小于0")
    @Schema(description = "查询得到的现有名额记录版本", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long expectedVersion;
}
