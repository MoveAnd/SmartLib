package com.smartlib.book.modular.inventory.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "C端馆藏详情")
public class BookInventoryClientDetailResult {

    @Schema(description = "馆藏ID")
    private String id;

    @Schema(description = "出版版本标识")
    private String editionKey;

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "可借库存")
    private Integer availableStock;

    @Schema(description = "上架状态：ON_SHELF上架，OFF_SHELF下架")
    private String status;
}
