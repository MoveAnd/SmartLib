package com.smartlib.book.modular.favorite.controller;

import com.smartlib.book.modular.favorite.param.BookFavoriteParam;
import com.smartlib.book.modular.favorite.service.BookFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.auth.core.annotation.SaClientCheckLogin;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;

@Tag(name = "C端图书收藏接口")
@RestController
@Validated
@SaClientCheckLogin
public class BookFavoriteController {

    @Resource
    private BookFavoriteService bookFavoriteService;

    @Operation(summary = "收藏出版版本")
    @CommonLog("收藏图书")
    @PostMapping("/book/c/favorite/add")
    public CommonResult<String> add(@RequestBody @Valid BookFavoriteParam param) {
        bookFavoriteService.add(param, StpClientUtil.getLoginIdAsString());
        return CommonResult.ok();
    }

    @Operation(summary = "取消收藏出版版本")
    @CommonLog("取消收藏图书")
    @PostMapping("/book/c/favorite/cancel")
    public CommonResult<String> cancel(@RequestBody @Valid BookFavoriteParam param) {
        bookFavoriteService.cancel(param, StpClientUtil.getLoginIdAsString());
        return CommonResult.ok();
    }
}
