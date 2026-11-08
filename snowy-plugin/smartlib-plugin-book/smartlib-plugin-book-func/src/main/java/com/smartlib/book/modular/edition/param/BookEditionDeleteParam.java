package com.smartlib.book.modular.edition.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 出版版本删除参数。 */
@Getter
@Setter
public class BookEditionDeleteParam {

    @NotBlank(message = "key不能为空")
    @Size(max = 64, message = "key长度不能超过64")
    @Schema(description = "待删除版本的主键", example = "/books/OL7353617M", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;
}
