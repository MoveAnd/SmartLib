package com.smartlib.book.modular.edition.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 出版版本基础信息编辑参数。未提交或 null 保持原值，列表整体替换，空列表表示清空。
 */
@Getter
@Setter
public class BookEditionEditParam {

    @NotBlank(message = "key不能为空")
    @Size(max = 64, message = "key长度不能超过64")
    @Schema(description = "待编辑版本的主键", example = "/books/OL7353617M", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;

    @Schema(description = "版本书名，不允许空白或清空")
    private String title;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "出版社，整体替换")
    private List<@NotBlank(message = "出版社不能为空") String> publishers;

    @Schema(description = "出版日期，保留来源中的日期文本格式")
    private String publishDate;

    @Schema(description = "语言标识，整体替换", example = "[\"/languages/zho\"]")
    private List<@NotBlank(message = "语言标识不能为空") String> languages;

    @Schema(description = "页数")
    @Positive(message = "页数必须大于0")
    private Integer numberOfPages;

    @Schema(description = "封面ID，整体替换")
    private List<@NotNull(message = "封面ID不能为空") @Positive(message = "封面ID必须大于0") Long> covers;

    @Schema(description = "明确清空的可选字段名：subtitle、description、publishers、publishDate、languages、numberOfPages、covers；同一字段不能同时赋值和清空")
    private List<@NotBlank(message = "清空字段名不能为空") String> clearFields;
}
