package com.smartlib.book.feign.provider.inventory;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartlib.book.feign.BookInventoryFeign;
import com.smartlib.book.modular.inventory.service.BookInventoryService;
import com.smartlib.idempotent.support.IdempotentRequestExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.seata.core.context.RootContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.Map;

/** 馆藏归还事务参与方。 */
@RequiredArgsConstructor
@RestController
@Validated
@SaCheckLogin
public class BookInventoryFeignProvider implements BookInventoryFeign {

    private final BookInventoryService bookInventoryService;
    private final IdempotentRequestExecutor idempotentRequestExecutor;
    private final ObjectMapper objectMapper;

    @Override
    public CommonResult<String> returnStock(String inventoryId, String borrowId, String idempotencyKey) {
        // 在幂等执行器之前检查，库存接口只能参与全局事务，不能自行发起。
        if (!RootContext.inGlobalTransaction()) {
            throw new CommonException("归还库存必须参与全局事务");
        }
        idempotentRequestExecutor.execute("B:" + StpUtil.getLoginIdAsString(), "RETURN_STOCK", idempotencyKey,
                Map.of("inventoryId", inventoryId, "borrowId", borrowId), () -> {
                    bookInventoryService.returnStock(inventoryId);
                    ObjectNode response = objectMapper.valueToTree(CommonResult.ok());
                    response.remove("traceId");
                    return response;
                });
        return CommonResult.ok();
    }
}
