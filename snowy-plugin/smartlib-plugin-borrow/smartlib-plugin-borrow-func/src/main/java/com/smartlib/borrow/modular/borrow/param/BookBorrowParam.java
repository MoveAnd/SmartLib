package com.smartlib.borrow.modular.borrow.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "批量借阅图书参数，每个馆藏借一本")
public class BookBorrowParam {

    @NotEmpty(message = "馆藏ID列表不能为空")
    @Size(max = 100, message = "一次最多借阅100个馆藏")
    @Schema(description = "馆藏ID列表，不允许重复")
    private List<@NotBlank(message = "馆藏ID不能为空")
            @Size(max = 20, message = "馆藏ID长度不能超过20") String> inventoryIds;
}
