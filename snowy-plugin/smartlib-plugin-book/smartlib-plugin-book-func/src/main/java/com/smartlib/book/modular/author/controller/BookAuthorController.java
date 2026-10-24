package com.smartlib.book.modular.author.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.author.param.BookAuthorPageParam;
import com.smartlib.book.modular.author.result.BookAuthorDetailResult;
import com.smartlib.book.modular.author.result.BookAuthorSummaryResult;
import com.smartlib.book.modular.author.service.BookAuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.pojo.CommonResult;

@Tag(name = "图书作者接口")
@RestController
@Validated
public class BookAuthorController {

    /** 作者查询服务 */
    @Resource
    private BookAuthorService bookAuthorService;

    @Operation(summary = "作者分页搜索")
    @GetMapping("/book/author/page")
    public CommonResult<Page<BookAuthorSummaryResult>> page(@Valid BookAuthorPageParam param) {
        return CommonResult.data(bookAuthorService.page(param));
    }

    @Operation(summary = "作者详情")
    @GetMapping("/book/author/detail")
    public CommonResult<BookAuthorDetailResult> detail(
            @Parameter(description = "作者主键", example = "/authors/OL23919A", required = true)
            @RequestParam("key") @NotBlank(message = "key不能为空") String key) {
        return CommonResult.data(bookAuthorService.detail(key));
    }
}
