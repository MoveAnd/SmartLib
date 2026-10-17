package com.smartlib.book.modular.inventory.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** 新增馆藏参数。 */
@Getter
@Setter
public class BookInventoryAddParam {

    /** 出版版本标识 */
    @NotBlank(message = "editionKey不能为空")
    @Schema(description = "出版版本标识", example = "/books/OL7353617M", requiredMode = Schema.RequiredMode.REQUIRED)
    private String editionKey;

    /** 总库存 */
    @NotNull(message = "totalStock不能为空")
    @Min(value = 0, message = "总库存不能小于0")
    @Schema(description = "总库存，可借库存初始化为该值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalStock;
}
