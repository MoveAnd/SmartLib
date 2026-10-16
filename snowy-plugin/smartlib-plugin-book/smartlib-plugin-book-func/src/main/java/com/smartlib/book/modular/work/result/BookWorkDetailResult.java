package com.smartlib.book.modular.work.result;

import com.smartlib.book.modular.author.result.BookAuthorSummaryResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "作品详情")
public class BookWorkDetailResult extends BookWorkSummaryResult {

    /** 作品简介 */
    @Schema(description = "作品简介")
    private String description;

    /** 封面ID列表 */
    @Schema(description = "封面ID列表")
    private List<Long> covers;

    /** 作品主题列表 */
    @Schema(description = "作品主题列表")
    private List<String> subjects;

    /** 首次出版日期，保留来源文本 */
    @Schema(description = "首次出版日期，保留来源文本")
    private String firstPublishDate;

    /** 作者摘要列表 */
    @Schema(description = "作者摘要列表")
    private List<BookAuthorSummaryResult> authors;
}
