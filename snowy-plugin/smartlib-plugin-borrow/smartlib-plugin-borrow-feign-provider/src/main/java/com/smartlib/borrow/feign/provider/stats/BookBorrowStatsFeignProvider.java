package com.smartlib.borrow.feign.provider.stats;

import com.smartlib.borrow.feign.BookBorrowStatsFeign;
import com.smartlib.borrow.modular.stats.service.BookBorrowStatsService;
import lombok.RequiredArgsConstructor;
import org.apache.seata.core.context.RootContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.exception.CommonException;

/** 馆藏创建全局事务的统计初始化参与方。 */
@RequiredArgsConstructor
@RestController
@Validated
public class BookBorrowStatsFeignProvider implements BookBorrowStatsFeign {
    private final BookBorrowStatsService bookBorrowStatsService;

    @Override
    public void initialize(String editionKey) {
        if (!RootContext.inGlobalTransaction()) {
            throw new CommonException("借阅统计初始化必须参与馆藏创建的全局事务");
        }
        bookBorrowStatsService.initialize(editionKey);
    }
}
