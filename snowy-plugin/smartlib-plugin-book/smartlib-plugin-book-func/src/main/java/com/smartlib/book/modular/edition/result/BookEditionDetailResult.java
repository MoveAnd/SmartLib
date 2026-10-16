package com.smartlib.book.modular.edition.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "出版版本详情")
public class BookEditionDetailResult extends BookEditionSummaryResult {

    /** 副标题 */
    @Schema(description = "副标题")
    private String subtitle;

    /** 版本简介 */
    @Schema(description = "版本简介")
    private String description;

    /** 出版社列表 */
    @Schema(description = "出版社列表")
    private List<String> publishers;

    /** 出版日期，保留来源文本 */
    @Schema(description = "出版日期，保留来源文本")
    private String publishDate;

    /** 语言标识列表 */
    @Schema(description = "语言标识列表，例如/languages/eng")
    private List<String> languages;

    /** 页数 */
    @Schema(description = "页数")
    private Integer numberOfPages;

    /** 封面ID列表 */
    @Schema(description = "封面ID列表")
    private List<Long> covers;

    /** 有效ISBN列表 */
    @Schema(description = "清洗后的有效ISBN列表")
    private List<String> isbns;
}
