package com.smartlib.book.modular.favorite.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartlib.book.modular.favorite.entity.BookFavoriteStats;
import com.smartlib.book.modular.favorite.result.BookFavoriteRankingResult;

import java.util.List;

public interface BookFavoriteStatsService extends IService<BookFavoriteStats> {

    /**
     * 从 Redis 读取收藏 TOP10 的排名、出版版本标识和当前收藏人数。
     */
    List<BookFavoriteRankingResult> top10();

    /**
     * 增加一名有效收藏读者，首次计数时初始化统计记录。
     */
    void increase(String editionKey);

    /**
     * 减少一名有效收藏读者，统计缺失或人数不足时失败并回滚调用事务。
     */
    void decrease(String editionKey);
}
