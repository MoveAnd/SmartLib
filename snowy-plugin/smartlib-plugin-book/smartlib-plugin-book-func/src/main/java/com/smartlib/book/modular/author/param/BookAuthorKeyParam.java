package com.smartlib.book.modular.author.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** 作者主键参数。 */
@Getter
@Setter
public class BookAuthorKeyParam {

    /** 作者主键 */
    @NotBlank(message = "key不能为空")
    @Schema(description = "作者主键", example = "/authors/OL23919A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;
}
