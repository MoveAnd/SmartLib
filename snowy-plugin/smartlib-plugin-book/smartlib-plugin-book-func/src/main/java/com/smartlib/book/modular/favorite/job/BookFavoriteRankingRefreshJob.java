package com.smartlib.book.modular.favorite.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.favorite.entity.BookFavoriteStats;
import com.smartlib.book.modular.favorite.mapper.BookFavoriteStatsMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 从当前收藏人数统计表生成 Redis 收藏 TOP10 快照。
 */
@Component
@RequiredArgsConstructor
public class BookFavoriteRankingRefreshJob {

    public static final String RANKING_KEY = "book:rank:favorite:edition:top10";

    // 以榜单键作为临时键的哈希标签，使两个键落在同一个 Redis Cluster 槽位。
    private static final String STAGING_KEY = "{" + RANKING_KEY + "}:staging";
    private static final String REFRESH_LOCK_KEY = RANKING_KEY + ":refresh-lock";

    private final BookFavoriteStatsMapper bookFavoriteStatsMapper;
    private final RedissonClient redissonClient;

    private String replaceRankingScript;

    @PostConstruct
    private void loadRankingScript() throws IOException {
        ClassPathResource resource = new ClassPathResource("lua/replace-favorite-ranking.lua");
        replaceRankingScript = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    public void refresh() {
        RLock refreshLock = redissonClient.getLock(REFRESH_LOCK_KEY);
        if (!refreshLock.tryLock()) {
            return;
        }

        try {
            LambdaQueryWrapper<BookFavoriteStats> query = new LambdaQueryWrapper<BookFavoriteStats>()
                    .select(BookFavoriteStats::getEditionKey, BookFavoriteStats::getFavoriteCount)
                    .gt(BookFavoriteStats::getFavoriteCount, 0)
                    .orderByDesc(BookFavoriteStats::getFavoriteCount)
                    .orderByDesc(BookFavoriteStats::getEditionKey);
            List<BookFavoriteStats> top10 = bookFavoriteStatsMapper
                    .selectPage(new Page<BookFavoriteStats>(1, 10, false), query).getRecords();
            List<Object> arguments = new ArrayList<>(top10.size() * 2);
            for (BookFavoriteStats stats : top10) {
                arguments.add(Long.toString(stats.getFavoriteCount()));
                arguments.add(stats.getEditionKey());
            }
            redissonClient.getScript(StringCodec.INSTANCE).eval(RScript.Mode.READ_WRITE,
                    replaceRankingScript, RScript.ReturnType.INTEGER,
                    List.of(RANKING_KEY, STAGING_KEY), arguments.toArray());
        } finally {
            refreshLock.unlock();
        }
    }
}
