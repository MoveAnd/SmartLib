package com.smartlib.borrow.modular.stats.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlib.borrow.modular.stats.entity.BookBorrowStats;
import com.smartlib.borrow.modular.stats.mapper.BookBorrowStatsMapper;
import com.smartlib.borrow.modular.stats.service.BookBorrowStatsService;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookBorrowStatsServiceImpl extends ServiceImpl<BookBorrowStatsMapper, BookBorrowStats>
        implements BookBorrowStatsService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initialize(String editionKey) {
        if (this.getById(editionKey) != null) {
            return;
        }
        BookBorrowStats stats = new BookBorrowStats();
        stats.setEditionKey(editionKey);
        stats.setBorrowCount(0L);
        try {
            this.save(stats);
        } catch (DuplicateKeyException e) {
            // edition_key 是本表唯一键；并发请求已创建时保持原次数和审计字段不变。
        }
    }
}
