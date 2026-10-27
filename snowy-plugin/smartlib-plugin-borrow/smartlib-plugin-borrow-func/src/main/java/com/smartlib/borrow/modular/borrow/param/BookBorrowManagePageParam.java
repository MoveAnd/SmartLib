package com.smartlib.borrow.modular.borrow.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 管理端借阅记录分页查询参数。 */
@Getter
@Setter
public class BookBorrowManagePageParam extends BookBorrowPageParam {

    @Size(max = 20, message = "userId长度不能超过20")
    @Schema(description = "C端读者ID")
    private String userId;
}
