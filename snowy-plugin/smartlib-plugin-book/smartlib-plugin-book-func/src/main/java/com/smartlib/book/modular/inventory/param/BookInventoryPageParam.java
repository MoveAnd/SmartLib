package com.smartlib.book.modular.inventory.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/** 馆藏库存分页查询参数。 */
@Getter
@Setter
public class BookInventoryPageParam {

    /** 当前页 */
    @Schema(description = "当前页码")
    private Integer current;

    /** 每页条数 */
    @Schema(description = "每页条数")
    private Integer size;

    /** 排序字段 */
    @Schema(description = "排序字段，字段驼峰名称，如：createTime、totalStock、availableStock")
    private String sortField;

    /** 排序方式 */
    @Schema(description = "排序方式，升序：ascend；降序：descend")
    private String sortOrder;

    /** 版本书名关键词 */
    @Schema(description = "版本书名关键词")
    private String searchKey;

    /** 出版版本标识 */
    @Schema(description = "出版版本标识", example = "/books/OL7353617M")
    private String editionKey;

    /** 上架状态 */
    @Schema(description = "上架状态：ON_SHELF上架，OFF_SHELF下架")
    private String status;
}
