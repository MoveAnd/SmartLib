package com.smartlib.book.modular.favorite.controller;

import com.smartlib.book.modular.favorite.result.BookFavoriteRankingResult;
import com.smartlib.book.modular.favorite.service.BookFavoriteStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.annotation.SaClientCheckLogin;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.List;

@RestController
@SaClientCheckLogin
@RequiredArgsConstructor
@Tag(name = "C端收藏排行榜接口")
public class BookFavoriteRankingController {

    private final BookFavoriteStatsService bookFavoriteStatsService;

    @Operation(summary = "查询出版版本收藏TOP10")
    @GetMapping("/book/c/rank/edition/top10")
    public CommonResult<List<BookFavoriteRankingResult>> top10() {
        return CommonResult.data(bookFavoriteStatsService.top10());
    }
}
