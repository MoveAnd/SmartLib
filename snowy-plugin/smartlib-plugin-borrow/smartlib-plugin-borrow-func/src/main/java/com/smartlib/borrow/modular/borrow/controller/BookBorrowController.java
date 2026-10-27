package com.smartlib.borrow.modular.borrow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.borrow.modular.borrow.param.BookBorrowPageParam;
import com.smartlib.borrow.modular.borrow.result.BookBorrowResult;
import com.smartlib.borrow.modular.borrow.service.BookBorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.annotation.SaClientCheckLogin;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.common.pojo.CommonResult;

@Tag(name = "C端借阅记录接口")
@RestController
@Validated
@SaClientCheckLogin
public class BookBorrowController {

    @Resource
    private BookBorrowService bookBorrowService;

    @Operation(summary = "分页查询本人借阅记录")
    @GetMapping("/borrow/c/record/page")
    public CommonResult<Page<BookBorrowResult>> page(@Valid BookBorrowPageParam param) {
        return CommonResult.data(bookBorrowService.clientPage(param, StpClientUtil.getLoginIdAsString()));
    }

    @Operation(summary = "查询本人借阅记录详情")
    @GetMapping("/borrow/c/record/detail")
    public CommonResult<BookBorrowResult> detail(
            @Parameter(description = "借阅记录ID", required = true)
            @RequestParam("id") @NotBlank(message = "id不能为空")
            @Size(max = 20, message = "id长度不能超过20") String id) {
        return CommonResult.data(bookBorrowService.clientDetail(id, StpClientUtil.getLoginIdAsString()));
    }
}
