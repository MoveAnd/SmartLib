package com.smartlib.book.modular.edition.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.edition.param.BookEditionKeyParam;
import com.smartlib.book.modular.edition.param.BookEditionIsbnParam;
import com.smartlib.book.modular.edition.param.BookEditionPageParam;
import com.smartlib.book.modular.edition.result.BookEditionDetailResult;
import com.smartlib.book.modular.edition.result.BookEditionSummaryResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.List;

@Tag(name = "图书出版版本接口")
@RestController
@Validated
public class BookEditionController {

    @Operation(summary = "作品下的版本分页列表")
    @GetMapping("/book/edition/page")
    public CommonResult<Page<BookEditionSummaryResult>> page(@Valid BookEditionPageParam param) {
        return CommonResult.get(501, "接口暂未实现", null);
    }

    @Operation(summary = "出版版本详情")
    @GetMapping("/book/edition/detail")
    public CommonResult<BookEditionDetailResult> detail(@Valid BookEditionKeyParam param) {
        return CommonResult.get(501, "接口暂未实现", null);
    }

    @Operation(summary = "通过ISBN查询版本列表")
    @GetMapping("/book/edition/byIsbn")
    public CommonResult<List<BookEditionSummaryResult>> byIsbn(@Valid BookEditionIsbnParam param) {
        return CommonResult.get(501, "接口暂未实现", null);
    }
}
