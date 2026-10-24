package com.smartlib.book.modular.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.inventory.param.BookInventoryAddParam;
import com.smartlib.book.modular.inventory.param.BookInventoryAdjustStockParam;
import com.smartlib.book.modular.inventory.param.BookInventoryIdParam;
import com.smartlib.book.modular.inventory.param.BookInventoryPageParam;
import com.smartlib.book.modular.inventory.result.BookInventoryResult;
import com.smartlib.book.modular.inventory.service.BookInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.pojo.CommonResult;

@Tag(name = "馆藏库存管理接口")
@RestController
@Validated
public class BookInventoryController {

    @Resource
    private BookInventoryService bookInventoryService;

    @Operation(summary = "馆藏库存分页查询")
    @GetMapping("/book/inventory/page")
    public CommonResult<Page<BookInventoryResult>> page(@Valid BookInventoryPageParam param) {
        return CommonResult.data(bookInventoryService.page(param));
    }

    @Operation(summary = "馆藏库存详情")
    @GetMapping("/book/inventory/detail")
    public CommonResult<BookInventoryResult> detail(
            @Parameter(description = "馆藏ID", required = true)
            @RequestParam("id") @NotBlank(message = "id不能为空") String id) {
        return CommonResult.data(bookInventoryService.detail(id));
    }

    @Operation(summary = "新增馆藏")
    @PostMapping("/book/inventory/add")
    public CommonResult<String> add(@RequestBody @Valid BookInventoryAddParam param) {
        bookInventoryService.add(param);
        return CommonResult.ok();
    }

    @Operation(summary = "调整馆藏总库存")
    @PostMapping("/book/inventory/adjustStock")
    public CommonResult<String> adjustStock(@RequestBody @Valid BookInventoryAdjustStockParam param) {
        bookInventoryService.adjustStock(param);
        return CommonResult.ok();
    }

    @Operation(summary = "馆藏上架")
    @PostMapping("/book/inventory/onShelf")
    public CommonResult<String> onShelf(@RequestBody @Valid BookInventoryIdParam param) {
        bookInventoryService.onShelf(param.getId());
        return CommonResult.ok();
    }

    @Operation(summary = "馆藏下架")
    @PostMapping("/book/inventory/offShelf")
    public CommonResult<String> offShelf(@RequestBody @Valid BookInventoryIdParam param) {
        bookInventoryService.offShelf(param.getId());
        return CommonResult.ok();
    }
}
