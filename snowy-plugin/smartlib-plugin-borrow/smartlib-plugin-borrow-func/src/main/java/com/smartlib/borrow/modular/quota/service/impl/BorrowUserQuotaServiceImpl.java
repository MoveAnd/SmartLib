package com.smartlib.borrow.modular.quota.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlib.borrow.modular.quota.entity.BorrowUserQuota;
import com.smartlib.borrow.modular.quota.mapper.BorrowUserQuotaMapper;
import com.smartlib.borrow.modular.quota.result.BorrowUserQuotaDetailResult;
import com.smartlib.borrow.modular.quota.service.BorrowUserQuotaService;
import org.springframework.stereotype.Service;

@Service
public class BorrowUserQuotaServiceImpl extends ServiceImpl<BorrowUserQuotaMapper, BorrowUserQuota>
        implements BorrowUserQuotaService {

    @Override
    public BorrowUserQuotaDetailResult detail(String userId) {
        BorrowUserQuota quota = this.getById(userId);
        BorrowUserQuotaDetailResult result = new BorrowUserQuotaDetailResult();
        result.setUserId(userId);
        result.setBorrowLimit(quota == null ? 0 : quota.getBorrowLimit());
        result.setBorrowedCount(quota == null ? 0 : quota.getBorrowedCount());
        result.setRemainingCount(Math.max(result.getBorrowLimit() - result.getBorrowedCount(), 0));
        return result;
    }
}
