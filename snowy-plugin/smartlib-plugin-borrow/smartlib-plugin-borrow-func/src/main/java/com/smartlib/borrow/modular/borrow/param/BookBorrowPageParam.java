package com.smartlib.borrow.modular.borrow.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 读者本人借阅记录分页查询参数，不接受外部指定读者身份。 */
@Getter
@Setter
public class BookBorrowPageParam {

    @Min(value = 1, message = "current不能小于1")
    @Schema(description = "当前页码")
    private Integer current;

    @Min(value = 1, message = "size不能小于1")
    @Schema(description = "每页条数，最大100")
    private Integer size;

    @Schema(description = "排序字段：id、borrowTime、dueTime、returnTime、createTime")
    private String sortField;

    @Schema(description = "排序方式，升序：ascend；降序：descend，与sortField同时传入")
    private String sortOrder;

    @Size(max = 20, message = "inventoryId长度不能超过20")
    @Schema(description = "馆藏ID")
    private String inventoryId;

    @Schema(description = "借阅状态：BORROWING未还、RETURNED已还")
    private String status;
}
