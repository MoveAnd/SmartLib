package com.smartlib.book.modular.edition.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookEditionIsbnParam {

    /** ISBN编号 */
    @NotBlank(message = "ISBN不能为空")
    @Size(max = 32, message = "ISBN输入过长")
    @Schema(description = "ISBN-10或ISBN-13，允许空格及连字符", example = "9780140328721")
    private String isbn;
}
