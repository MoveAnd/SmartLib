package com.smartlib.borrow.modular.stats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartlib.borrow.modular.stats.entity.BookBorrowStats;
import com.smartlib.borrow.modular.stats.result.BookBorrowRankingResult;

import java.util.List;

public interface BookBorrowStatsService extends IService<BookBorrowStats> {
    /** 从 Redis 读取借阅 TOP10 的排名、出版版本标识和累计次数。 */
    List<BookBorrowRankingResult> top10();

    /** 创建出版版本的零次借阅统计，已有记录保持不变。 */
    void initialize(String editionKey);

    /** 在借阅事务中累加出版版本的成功借阅次数。 */
    void addSuccessfulBorrows(String editionKey, int count);
}
