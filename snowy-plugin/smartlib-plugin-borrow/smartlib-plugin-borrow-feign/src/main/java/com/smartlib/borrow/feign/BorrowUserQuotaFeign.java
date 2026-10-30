package com.smartlib.borrow.feign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vip.xiaonuo.common.consts.FeignConstant;

/** 读者额度初始化事务接口。 */
@FeignClient(name = FeignConstant.BORROW_APP, contextId = "BorrowUserQuotaFeign")
public interface BorrowUserQuotaFeign {

    /** 账户创建时参与全局事务初始化额度，已有记录保持不变。 */
    @PostMapping("/feign/borrow/quota/initialize")
    void initialize(@RequestParam("userId") @NotBlank(message = "userId不能为空")
            @Size(max = 20, message = "userId长度不能超过20") String userId);
}
