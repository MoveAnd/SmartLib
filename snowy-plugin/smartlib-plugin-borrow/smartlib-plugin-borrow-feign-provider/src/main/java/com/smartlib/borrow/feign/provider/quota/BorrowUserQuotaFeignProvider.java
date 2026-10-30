package com.smartlib.borrow.feign.provider.quota;

import com.smartlib.borrow.feign.BorrowUserQuotaFeign;
import com.smartlib.borrow.modular.quota.service.BorrowUserQuotaService;
import lombok.RequiredArgsConstructor;
import org.apache.seata.core.context.RootContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.exception.CommonException;

/** 账户创建事务的额度初始化参与方，不要求读者已登录。 */
@RequiredArgsConstructor
@RestController
@Validated
public class BorrowUserQuotaFeignProvider implements BorrowUserQuotaFeign {

    private final BorrowUserQuotaService borrowUserQuotaService;

    @Override
    public void initialize(String userId) {
        if (!RootContext.inGlobalTransaction()) {
            throw new CommonException("额度初始化必须参与账户创建的全局事务");
        }
        borrowUserQuotaService.initialize(userId);
    }
}
