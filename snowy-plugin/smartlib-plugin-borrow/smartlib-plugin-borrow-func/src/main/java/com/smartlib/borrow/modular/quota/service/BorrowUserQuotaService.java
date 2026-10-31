package com.smartlib.borrow.modular.quota.service;

import com.smartlib.borrow.modular.quota.param.BorrowUserQuotaAdjustLimitParam;
import com.smartlib.borrow.modular.quota.result.BorrowUserQuotaDetailResult;

public interface BorrowUserQuotaService {

    BorrowUserQuotaDetailResult detail(String userId);

    /** 账户创建事务中初始化为5本额度，已有记录保持不变。 */
    void initialize(String userId);

    BorrowUserQuotaDetailResult adjustLimit(BorrowUserQuotaAdjustLimitParam param);

    /** 确认归还后释放一个已占用的借阅名额。 */
    void releaseQuota(String userId);

    /** 批量占用借阅名额，同时锁定读者额度行直到事务结束。 */
    void occupyQuota(String userId, int count);
}
