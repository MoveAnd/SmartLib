package com.smartlib.borrow.modular.stats.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlib.borrow.modular.stats.entity.BookBorrowStats;
import com.smartlib.borrow.modular.stats.job.BookBorrowRankingRefreshJob;
import com.smartlib.borrow.modular.stats.result.BookBorrowRankingResult;
import com.smartlib.borrow.modular.stats.mapper.BookBorrowStatsMapper;
import com.smartlib.borrow.modular.stats.service.BookBorrowStatsService;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.common.exception.CommonException;
import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.ScoredEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class BookBorrowStatsServiceImpl extends ServiceImpl<BookBorrowStatsMapper, BookBorrowStats>
        implements BookBorrowStatsService {
    @Resource
    private RedissonClient redissonClient;

    @Override
    public List<BookBorrowRankingResult> top10() {
        Collection<ScoredEntry<String>> entries = redissonClient
                .<String>getScoredSortedSet(BookBorrowRankingRefreshJob.RANKING_KEY, StringCodec.INSTANCE)
                .entryRangeReversed(0, 9);
        if (entries.isEmpty()) {
            return List.of();
        }
        List<BookBorrowRankingResult> results = new ArrayList<>(entries.size());
        int rank = 1;
        for (ScoredEntry<String> entry : entries) {
            BookBorrowRankingResult result = new BookBorrowRankingResult();
            result.setRank(rank);
            result.setEditionKey(entry.getValue());
            result.setBorrowCount(entry.getScore().longValue());
            results.add(result);
            rank++;
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSuccessfulBorrows(String editionKey, int count) {
        initialize(editionKey);
        BookBorrowStats stats = new BookBorrowStats();
        stats.setEditionKey(editionKey);
        int updatedRows = baseMapper.addSuccessfulBorrows(stats, count);
        if (updatedRows != 1) {
            throw new CommonException("无法累加出版版本的借阅次数：{}", editionKey);
        }
    }

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
