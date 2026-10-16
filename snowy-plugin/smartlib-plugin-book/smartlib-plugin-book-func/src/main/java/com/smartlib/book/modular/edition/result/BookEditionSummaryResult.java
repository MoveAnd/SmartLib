package com.smartlib.book.modular.edition.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "出版版本摘要")
public class BookEditionSummaryResult {

    /** 版本主键 */
    @Schema(description = "版本主键")
    private String key;

    /** 版本书名 */
    @Schema(description = "版本书名")
    private String title;

    /** 所属作品标识 */
    @Schema(description = "所属作品标识")
    private String workKey;

    /** Internet Archive条目标识 */
    @Schema(description = "Internet Archive条目标识")
    private String ocaid;
}
