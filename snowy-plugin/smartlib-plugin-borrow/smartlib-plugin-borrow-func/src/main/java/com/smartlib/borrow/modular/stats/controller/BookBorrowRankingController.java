package com.smartlib.borrow.modular.stats.controller;

import com.smartlib.borrow.modular.stats.result.BookBorrowRankingResult;
import com.smartlib.borrow.modular.stats.service.BookBorrowStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.annotation.SaClientCheckLogin;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.List;

@RestController
@SaClientCheckLogin
@Tag(name = "C端借阅排行榜接口")
public class BookBorrowRankingController {
    @Resource
    private BookBorrowStatsService bookBorrowStatsService;

    @Operation(summary = "查询出版版本累计借阅TOP10")
    @GetMapping("/borrow/c/rank/edition/top10")
    public CommonResult<List<BookBorrowRankingResult>> top10() {
        return CommonResult.data(bookBorrowStatsService.top10());
    }
}
