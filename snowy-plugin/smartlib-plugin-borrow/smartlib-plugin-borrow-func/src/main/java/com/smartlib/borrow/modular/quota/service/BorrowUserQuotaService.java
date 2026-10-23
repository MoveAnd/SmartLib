package com.smartlib.borrow.modular.quota.service;

import com.smartlib.borrow.modular.quota.result.BorrowUserQuotaDetailResult;

public interface BorrowUserQuotaService {

    BorrowUserQuotaDetailResult detail(String userId);
}
