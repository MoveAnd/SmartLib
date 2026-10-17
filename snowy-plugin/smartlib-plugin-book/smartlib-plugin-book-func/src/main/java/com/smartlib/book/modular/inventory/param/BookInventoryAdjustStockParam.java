package com.smartlib.book.modular.inventory.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** 馆藏总库存调整参数。 */
@Getter
@Setter
public class BookInventoryAdjustStockParam {

    /** 馆藏ID */
    @NotBlank(message = "id不能为空")
    @Schema(description = "馆藏ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** 调整后的总库存 */
    @NotNull(message = "totalStock不能为空")
    @Min(value = 0, message = "总库存不能小于0")
    @Schema(description = "调整后的总库存，不得小于已借出数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalStock;
}
