package com.smartlib.book.modular.edition.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** 出版版本分页查询参数。 */
@Getter
@Setter
public class BookEditionPageParam {

    /** 当前页 */
    @Schema(description = "当前页码")
    private Integer current;

    /** 每页条数 */
    @Schema(description = "每页条数")
    private Integer size;

    /** 排序字段 */
    @Schema(description = "排序字段，字段驼峰名称，如：title")
    private String sortField;

    /** 排序方式 */
    @Schema(description = "排序方式，升序：ascend；降序：descend")
    private String sortOrder;

    /** 版本书名关键词 */
    @Schema(description = "版本书名关键词")
    private String searchKey;

    /** 作品标识 */
    @NotBlank(message = "workKey不能为空")
    @Schema(description = "作品标识", example = "/works/OL45804W", requiredMode = Schema.RequiredMode.REQUIRED)
    private String workKey;
}
