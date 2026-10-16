package com.smartlib.book.modular.work.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "作品摘要")
public class BookWorkSummaryResult {

    /** 作品主键 */
    @Schema(description = "作品主键")
    private String key;

    /** 作品标题 */
    @Schema(description = "作品标题")
    private String title;
}
