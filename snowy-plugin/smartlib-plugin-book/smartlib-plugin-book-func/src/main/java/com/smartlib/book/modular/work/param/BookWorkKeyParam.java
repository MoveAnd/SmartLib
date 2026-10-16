package com.smartlib.book.modular.work.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** 作品主键参数。 */
@Getter
@Setter
public class BookWorkKeyParam {

    /** 作品主键 */
    @NotBlank(message = "key不能为空")
    @Schema(description = "作品主键", example = "/works/OL45804W", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;
}
