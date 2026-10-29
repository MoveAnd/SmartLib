package com.smartlib.borrow.modular.borrow.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookBorrowIdParam {

    @NotBlank(message = "id不能为空")
    @Size(max = 20, message = "id长度不能超过20")
    @Schema(description = "借阅记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;
}
