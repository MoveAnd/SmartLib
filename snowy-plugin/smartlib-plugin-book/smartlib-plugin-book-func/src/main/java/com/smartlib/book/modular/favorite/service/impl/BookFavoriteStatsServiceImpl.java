package com.smartlib.book.modular.favorite.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlib.book.modular.favorite.entity.BookFavoriteStats;
import com.smartlib.book.modular.favorite.job.BookFavoriteRankingRefreshJob;
import com.smartlib.book.modular.favorite.mapper.BookFavoriteStatsMapper;
import com.smartlib.book.modular.favorite.result.BookFavoriteRankingResult;
import com.smartlib.book.modular.favorite.service.BookFavoriteStatsService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.common.exception.CommonException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookFavoriteStatsServiceImpl extends ServiceImpl<BookFavoriteStatsMapper, BookFavoriteStats>
        implements BookFavoriteStatsService {

    private final RedissonClient redissonClient;

    @Override
    public List<BookFavoriteRankingResult> top10() {
        Collection<ScoredEntry<String>> entries = redissonClient
                .<String>getScoredSortedSet(BookFavoriteRankingRefreshJob.RANKING_KEY, StringCodec.INSTANCE)
                .entryRangeReversed(0, 9);
        if (entries.isEmpty()) {
            return List.of();
        }
        List<BookFavoriteRankingResult> results = new ArrayList<>(entries.size());
        int rank = 1;
        for (ScoredEntry<String> entry : entries) {
            BookFavoriteRankingResult result = new BookFavoriteRankingResult();
            result.setRank(rank);
            result.setEditionKey(entry.getValue());
            result.setFavoriteCount(entry.getScore().longValue());
            results.add(result);
            rank++;
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increase(String editionKey) {
        BookFavoriteStats stats = new BookFavoriteStats();
        stats.setEditionKey(editionKey);
        stats.setFavoriteCount(0L);
        baseMapper.insertIfAbsent(stats);
        if (baseMapper.increase(stats) != 1) {
            throw new CommonException("无法增加出版版本的收藏人数：{}", editionKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrease(String editionKey) {
        BookFavoriteStats stats = new BookFavoriteStats();
        stats.setEditionKey(editionKey);
        if (baseMapper.decrease(stats) != 1) {
            throw new CommonException("无法减少出版版本的收藏人数：{}", editionKey);
        }
    }
}
