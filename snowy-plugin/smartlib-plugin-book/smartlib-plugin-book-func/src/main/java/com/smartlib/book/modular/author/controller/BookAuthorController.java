package com.smartlib.book.modular.author.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.author.param.BookAuthorKeyParam;
import com.smartlib.book.modular.author.param.BookAuthorPageParam;
import com.smartlib.book.modular.author.result.BookAuthorDetailResult;
import com.smartlib.book.modular.author.result.BookAuthorSummaryResult;
import com.smartlib.book.modular.author.service.BookAuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
    public CommonResult<BookAuthorDetailResult> detail(@Valid BookAuthorKeyParam param) {
        return CommonResult.data(bookAuthorService.detail(param.getKey()));
    }
}
