package com.smartlib.borrow.feign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vip.xiaonuo.common.consts.FeignConstant;

/** 馆藏创建事务中的借阅统计初始化接口。 */
@FeignClient(name = FeignConstant.BORROW_APP, contextId = "BookBorrowStatsFeign")
public interface BookBorrowStatsFeign {
    /** 仅在缺失时创建零次统计，已有次数和审计字段保持不变。 */
    @PostMapping("/feign/borrow/stats/initialize")
    void initialize(@RequestParam("editionKey") @NotBlank(message = "editionKey不能为空")
            @Size(max = 64, message = "editionKey长度不能超过64") String editionKey);
}
