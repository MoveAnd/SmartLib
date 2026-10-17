package com.smartlib.book.modular.work.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.work.param.BookWorkKeyParam;
import com.smartlib.book.modular.work.param.BookWorkPageParam;
import com.smartlib.book.modular.work.result.BookWorkDetailResult;
import com.smartlib.book.modular.work.result.BookWorkSummaryResult;
import com.smartlib.book.modular.work.service.BookWorkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.pojo.CommonResult;

@Tag(name = "图书作品接口")
@RestController
@Validated
public class BookWorkController {

    @Resource
    private BookWorkService bookWorkService;

    @Operation(summary = "作品分页搜索")
    @GetMapping("/book/work/page")
    public CommonResult<Page<BookWorkSummaryResult>> page(@Valid BookWorkPageParam param) {
        return CommonResult.data(bookWorkService.page(param));
    }

    @Operation(summary = "作品详情")
    @GetMapping("/book/work/detail")
    public CommonResult<BookWorkDetailResult> detail(@Valid BookWorkKeyParam param) {
        return CommonResult.data(bookWorkService.detail(param.getKey()));
    }
}
