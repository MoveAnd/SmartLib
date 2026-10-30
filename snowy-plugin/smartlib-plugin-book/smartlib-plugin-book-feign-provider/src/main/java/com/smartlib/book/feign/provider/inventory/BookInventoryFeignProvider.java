package com.smartlib.book.feign.provider.inventory;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.smartlib.book.core.consts.IdempotentConstant;
import com.smartlib.book.feign.BookInventoryFeign;
import com.smartlib.book.modular.inventory.service.BookInventoryService;
import com.smartlib.idempotent.support.IdempotentOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.pojo.CommonResult;

/** 馆藏归还事务参与方。 */
@RequiredArgsConstructor
@RestController
@Validated
public class BookInventoryFeignProvider implements BookInventoryFeign {

    private final BookInventoryService bookInventoryService;

    @Override
    @SaCheckLogin
    @IdempotentOperation(value = IdempotentConstant.RETURN_STOCK, requestParamsOnly = true, participantOnly = true)
    public CommonResult<String> returnStock(String inventoryId, String borrowId, String idempotencyKey) {
        bookInventoryService.returnStock(inventoryId);
        return CommonResult.ok();
    }
}
