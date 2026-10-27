package com.smartlib.borrow.modular.borrow.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.borrow.modular.borrow.param.BookBorrowManagePageParam;
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
import vip.xiaonuo.common.pojo.CommonResult;

@Tag(name = "B端借阅记录管理接口")
@RestController
@Validated
@SaCheckLogin
public class BookBorrowManageController {

    @Resource
    private BookBorrowService bookBorrowService;

    @Operation(summary = "分页查询借阅记录")
    @GetMapping("/borrow/record/page")
    public CommonResult<Page<BookBorrowResult>> page(@Valid BookBorrowManagePageParam param) {
        return CommonResult.data(bookBorrowService.page(param));
    }

    @Operation(summary = "查询借阅记录详情")
    @GetMapping("/borrow/record/detail")
    public CommonResult<BookBorrowResult> detail(
            @Parameter(description = "借阅记录ID", required = true)
            @RequestParam("id") @NotBlank(message = "id不能为空")
            @Size(max = 20, message = "id长度不能超过20") String id) {
        return CommonResult.data(bookBorrowService.detail(id));
    }
}
