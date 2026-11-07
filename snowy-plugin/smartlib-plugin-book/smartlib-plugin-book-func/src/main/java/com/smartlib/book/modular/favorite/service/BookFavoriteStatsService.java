package com.smartlib.book.modular.favorite.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartlib.book.modular.favorite.entity.BookFavoriteStats;

public interface BookFavoriteStatsService extends IService<BookFavoriteStats> {

    /**
     * 增加一名有效收藏读者，首次计数时初始化统计记录。
     */
    void increase(String editionKey);

    /**
     * 减少一名有效收藏读者，统计缺失或人数不足时失败并回滚调用事务。
     */
    void decrease(String editionKey);
}
