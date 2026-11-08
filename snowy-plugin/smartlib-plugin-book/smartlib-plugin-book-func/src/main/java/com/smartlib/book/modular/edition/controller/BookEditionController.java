package com.smartlib.book.modular.edition.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.edition.param.BookEditionDeleteParam;
import com.smartlib.book.modular.edition.param.BookEditionEditParam;
import com.smartlib.book.modular.edition.param.BookEditionPageParam;
import com.smartlib.book.modular.edition.result.BookEditionDetailResult;
import com.smartlib.book.modular.edition.result.BookEditionSummaryResult;
import com.smartlib.book.modular.edition.service.BookEditionService;
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
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.List;

@Tag(name = "图书出版版本接口")
@RestController
@Validated
public class BookEditionController {

    @Resource
    private BookEditionService bookEditionService;

    @Operation(summary = "编辑出版版本基础信息")
    @SaCheckLogin
    @CommonLog("编辑出版版本")
    @PostMapping("/book/edition/edit")
    public CommonResult<String> edit(@RequestBody @Valid BookEditionEditParam param) {
        bookEditionService.edit(param);
        return CommonResult.ok();
    }

    @Operation(summary = "删除出版版本")
    @SaCheckLogin
    @CommonLog("删除出版版本")
    @PostMapping("/book/edition/delete")
    public CommonResult<String> delete(@RequestBody @Valid BookEditionDeleteParam param) {
        bookEditionService.delete(param);
        return CommonResult.ok();
    }

    @Operation(summary = "作品下的版本分页列表")
    @GetMapping("/book/edition/page")
    public CommonResult<Page<BookEditionSummaryResult>> page(@Valid BookEditionPageParam param) {
        return CommonResult.data(bookEditionService.page(param));
    }

    @Operation(summary = "出版版本详情")
    @GetMapping("/book/edition/detail")
    public CommonResult<BookEditionDetailResult> detail(
            @Parameter(description = "版本主键", example = "/books/OL7353617M", required = true)
            @RequestParam("key") @NotBlank(message = "key不能为空") String key) {
        return CommonResult.data(bookEditionService.detail(key));
    }

    @Operation(summary = "通过ISBN查询版本列表")
    @GetMapping("/book/edition/byIsbn")
    public CommonResult<List<BookEditionSummaryResult>> byIsbn(
            @Parameter(description = "ISBN-10或ISBN-13", example = "9780140328721", required = true)
            @RequestParam("isbn") @NotBlank(message = "ISBN不能为空") String isbn) {
        return CommonResult.data(bookEditionService.byIsbn(isbn));
    }
}
