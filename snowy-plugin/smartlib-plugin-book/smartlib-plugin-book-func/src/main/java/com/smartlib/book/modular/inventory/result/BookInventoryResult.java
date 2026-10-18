package com.smartlib.book.modular.inventory.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Schema(description = "馆藏库存查询结果")
public class BookInventoryResult {

    @Schema(description = "馆藏ID")
    private String id;

    @Schema(description = "出版版本标识")
    private String editionKey;

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "可借库存")
    private Integer availableStock;

    @Schema(description = "上架状态：ON_SHELF上架，OFF_SHELF下架")
    private String status;

    @Schema(description = "删除标志")
    private String deleteFlag;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "更新人名称")
    private String updateUserName;
}
