package com.smartlib.book.modular.author.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "作者详情")
public class BookAuthorDetailResult extends BookAuthorSummaryResult {

    /** 作者简介 */
    @Schema(description = "作者简介")
    private String bio;

    /** 出生日期，保留来源文本 */
    @Schema(description = "出生日期，保留来源文本")
    private String birthDate;

    /** 逝世日期，保留来源文本 */
    @Schema(description = "逝世日期，保留来源文本")
    private String deathDate;

    /** 维基百科链接 */
    @Schema(description = "维基百科链接")
    private String wikipedia;

    /** 作者别名列表 */
    @Schema(description = "作者别名列表")
    private List<String> alternateNames;

    /** 作者照片ID列表 */
    @Schema(description = "作者照片ID列表")
    private List<Long> photos;
}
