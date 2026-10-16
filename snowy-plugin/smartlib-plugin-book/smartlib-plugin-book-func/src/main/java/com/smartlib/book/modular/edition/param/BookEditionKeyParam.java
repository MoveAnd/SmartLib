package com.smartlib.book.modular.edition.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** 出版版本主键参数。 */
@Getter
@Setter
public class BookEditionKeyParam {

    /** 版本主键 */
    @NotBlank(message = "key不能为空")
    @Schema(description = "版本主键", example = "/books/OL7353617M", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;
}
