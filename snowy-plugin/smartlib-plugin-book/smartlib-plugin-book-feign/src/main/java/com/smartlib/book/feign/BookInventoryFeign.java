package com.smartlib.book.feign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import vip.xiaonuo.common.consts.FeignConstant;
import vip.xiaonuo.common.pojo.CommonResult;
import java.util.List;
import java.util.Map;

/** 馆藏库存事务接口。 */
@FeignClient(name = FeignConstant.BOOK_APP, contextId = "BookInventoryFeign")
public interface BookInventoryFeign {

    /** 查询本次借阅涉及的馆藏与出版版本关联。 */
    @PostMapping("/feign/book/inventory/editionKeys")
    CommonResult<Map<String, String>> editionKeys(
            @RequestParam("inventoryIds") @NotEmpty(message = "馆藏ID列表不能为空")
            @Size(max = 100, message = "一次最多查询100个馆藏")
            List<@NotBlank(message = "馆藏ID不能为空")
                    @Size(max = 20, message = "馆藏ID长度不能超过20") String> inventoryIds);

    /** 批量借出馆藏，每个馆藏扣减一本，必须参与调用方的全局事务。 */
    @PostMapping("/feign/book/inventory/borrowStock")
    CommonResult<String> borrowStock(
            @RequestParam("inventoryIds") @NotEmpty(message = "馆藏ID列表不能为空")
            @Size(max = 100, message = "一次最多借阅100个馆藏")
            List<@NotBlank(message = "馆藏ID不能为空")
                    @Size(max = 20, message = "馆藏ID长度不能超过20") String> inventoryIds,
            @RequestHeader("Idempotency-Key") String idempotencyKey);

    /** 确认归还时恢复一本馆藏库存，必须参与调用方的全局事务。 */
    @PostMapping("/feign/book/inventory/returnStock")
    CommonResult<String> returnStock(
            @RequestParam("inventoryId") @NotBlank(message = "inventoryId不能为空")
            @Size(max = 20, message = "inventoryId长度不能超过20") String inventoryId,
            @RequestParam("borrowId") @NotBlank(message = "borrowId不能为空")
            @Size(max = 20, message = "borrowId长度不能超过20") String borrowId,
            @RequestHeader("Idempotency-Key") String idempotencyKey);
}
