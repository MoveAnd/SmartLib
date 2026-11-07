package com.smartlib.book.modular.favorite.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 收藏与取消收藏参数。
 */
@Getter
@Setter
public class BookFavoriteParam {

    /**
     * 出版版本标识。
     */
    @NotBlank(message = "editionKey不能为空")
    @Size(max = 64, message = "editionKey长度不能超过64")
    @Schema(description = "出版版本标识", example = "/books/OL7353617M", requiredMode = Schema.RequiredMode.REQUIRED)
    private String editionKey;
}
