package com.smartlib.borrow.modular.stats.job;

import com.smartlib.borrow.modular.stats.entity.BookBorrowStats;
import com.smartlib.borrow.modular.stats.mapper.BookBorrowStatsMapper;
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
 * 从借阅统计表定时生成 Redis 借阅 TOP10 快照。
 */
@Component
@RequiredArgsConstructor
public class BookBorrowRankingRefreshJob {
    public static final String RANKING_KEY = "book:rank:borrow:edition:top10";

    // 以榜单键作为临时键的哈希标签，使两个键落在同一个 Redis Cluster 槽位。
    private static final String STAGING_KEY = "{" + RANKING_KEY + "}:staging";
    private static final String REFRESH_LOCK_KEY = RANKING_KEY + ":refresh-lock";

    private final BookBorrowStatsMapper bookBorrowStatsMapper;
    private final RedissonClient redissonClient;

    private String replaceRankingScript;

    @PostConstruct
    private void loadRankingScript() throws IOException {
        ClassPathResource resource = new ClassPathResource("lua/replace-borrow-ranking.lua");
        replaceRankingScript = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    public void refresh() {
        RLock refreshLock = redissonClient.getLock(REFRESH_LOCK_KEY);
        if (!refreshLock.tryLock()) {
            return;
        }

        try {
            List<BookBorrowStats> top10 = bookBorrowStatsMapper.selectBorrowTop10();
            List<Object> arguments = new ArrayList<>(top10.size() * 2);
            for (BookBorrowStats stats : top10) {
                arguments.add(Long.toString(stats.getBorrowCount()));
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
