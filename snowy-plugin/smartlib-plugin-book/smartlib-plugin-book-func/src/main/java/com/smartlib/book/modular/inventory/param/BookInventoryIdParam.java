package com.smartlib.book.modular.inventory.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** 馆藏主键参数。 */
@Getter
@Setter
public class BookInventoryIdParam {

    /** 馆藏ID */
    @NotBlank(message = "id不能为空")
    @Schema(description = "馆藏ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;
}
