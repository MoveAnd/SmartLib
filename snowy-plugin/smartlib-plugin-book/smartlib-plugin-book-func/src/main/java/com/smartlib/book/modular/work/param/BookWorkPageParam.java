package com.smartlib.book.modular.work.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/** 作品分页查询参数。 */
@Getter
@Setter
public class BookWorkPageParam {

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

    /** 作品标题关键词 */
    @Schema(description = "作品标题关键词")
    private String searchKey;

    /** 作者标识 */
    @Schema(description = "作者标识", example = "/authors/OL23919A")
    private String authorKey;
}
