package com.smartlib.borrow.modular.quota.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.smartlib.borrow.modular.quota.param.BorrowUserQuotaAdjustLimitParam;
import com.smartlib.borrow.modular.quota.result.BorrowUserQuotaDetailResult;
import com.smartlib.borrow.modular.quota.service.BorrowUserQuotaService;
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

@Tag(name = "B端个人借阅名额管理接口")
@RestController
@Validated
@SaCheckLogin
public class BorrowUserQuotaManageController {

    @Resource
    private BorrowUserQuotaService borrowUserQuotaService;

    @Operation(summary = "查询个人借阅名额")
    @GetMapping("/borrow/quota/detail")
    public CommonResult<BorrowUserQuotaDetailResult> detail(
            @Parameter(description = "C端读者ID", required = true)
            @RequestParam("userId") @NotBlank(message = "userId不能为空") String userId) {
        return CommonResult.data(borrowUserQuotaService.detail(userId));
    }

    @Operation(summary = "调整个人借阅上限")
    @CommonLog("调整个人借阅上限")
    @PostMapping("/borrow/quota/adjustLimit")
    public CommonResult<BorrowUserQuotaDetailResult> adjustLimit(
            @RequestBody @Valid BorrowUserQuotaAdjustLimitParam param) {
        return CommonResult.data(borrowUserQuotaService.adjustLimit(param));
    }
}
