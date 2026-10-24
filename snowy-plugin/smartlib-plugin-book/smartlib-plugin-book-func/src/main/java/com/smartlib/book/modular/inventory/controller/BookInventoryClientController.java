package com.smartlib.book.modular.inventory.controller;

import com.smartlib.book.modular.inventory.result.BookInventoryClientDetailResult;
import com.smartlib.book.modular.inventory.service.BookInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.annotation.SaClientCheckLogin;
import vip.xiaonuo.common.pojo.CommonResult;

@Tag(name = "C端馆藏查询接口")
@RestController
@Validated
@SaClientCheckLogin
public class BookInventoryClientController {

    @Resource
    private BookInventoryService bookInventoryService;

    @Operation(summary = "按出版版本查询馆藏详情")
    @GetMapping("/book/c/inventory/detail")
    public CommonResult<BookInventoryClientDetailResult> detail(
            @Parameter(description = "出版版本标识", example = "/books/OL7353617M", required = true)
            @RequestParam("editionKey")
            @NotBlank(message = "editionKey不能为空") String editionKey) {
        return CommonResult.data(bookInventoryService.clientDetail(editionKey));
    }
}
