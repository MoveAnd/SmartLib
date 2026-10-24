package com.smartlib.borrow.modular.quota.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BorrowUserQuotaDetailParam {

    @NotBlank(message = "userId不能为空")
    @Size(max = 20, message = "userId长度不能超过20")
    @Schema(description = "C端读者ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;
}
