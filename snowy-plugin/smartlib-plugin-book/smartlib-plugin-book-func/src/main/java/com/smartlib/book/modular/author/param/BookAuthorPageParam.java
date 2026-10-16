package com.smartlib.book.modular.author.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/** 作者分页查询参数。 */
@Getter
@Setter
public class BookAuthorPageParam {

    /** 当前页 */
    @Schema(description = "当前页码")
    private Integer current;

    /** 每页条数 */
    @Schema(description = "每页条数")
    private Integer size;

    /** 排序字段 */
    @Schema(description = "排序字段，字段驼峰名称，如：name")
    private String sortField;

    /** 排序方式 */
    @Schema(description = "排序方式，升序：ascend；降序：descend")
    private String sortOrder;

    /** 作者姓名关键词 */
    @Schema(description = "作者姓名关键词")
    private String searchKey;
}
