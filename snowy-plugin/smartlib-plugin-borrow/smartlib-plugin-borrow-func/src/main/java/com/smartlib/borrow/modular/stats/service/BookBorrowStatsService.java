package com.smartlib.borrow.modular.stats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartlib.borrow.modular.stats.entity.BookBorrowStats;

public interface BookBorrowStatsService extends IService<BookBorrowStats> {
    /** 创建出版版本的零次借阅统计，已有记录保持不变。 */
    void initialize(String editionKey);
}
