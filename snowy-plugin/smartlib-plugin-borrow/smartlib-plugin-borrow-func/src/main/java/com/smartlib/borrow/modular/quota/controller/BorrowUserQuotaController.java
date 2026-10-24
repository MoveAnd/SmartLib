package com.smartlib.borrow.modular.quota.controller;

import com.smartlib.borrow.modular.quota.result.BorrowUserQuotaDetailResult;
import com.smartlib.borrow.modular.quota.service.BorrowUserQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.annotation.SaClientCheckLogin;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.common.pojo.CommonResult;

@Tag(name = "C端借阅名额接口")
@RestController
@SaClientCheckLogin
public class BorrowUserQuotaController {

    @Resource
    private BorrowUserQuotaService borrowUserQuotaService;

    @Operation(summary = "查询本人借阅名额")
    @GetMapping("/borrow/c/quota/detail")
    public CommonResult<BorrowUserQuotaDetailResult> detail() {
        return CommonResult.data(borrowUserQuotaService.detail(StpClientUtil.getLoginIdAsString()));
    }
}
