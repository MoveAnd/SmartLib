package com.smartlib.book.modular.author.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "作者摘要")
public class BookAuthorSummaryResult {

    /** 作者主键 */
    @Schema(description = "作者主键")
    private String key;

    /** 作者姓名 */
    @Schema(description = "作者姓名")
    private String name;
}
