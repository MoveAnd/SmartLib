package com.smartlib.book.modular.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.smartlib.book.modular.favorite.entity.BookFavoriteStats;
import org.apache.ibatis.annotations.Param;

/**
 * book_favorite_stats 基础持久化接口。
 */
public interface BookFavoriteStatsMapper extends BaseMapper<BookFavoriteStats> {

    /**
     * 幂等创建零人数统计，保留 et 参数以生成公共审计字段。
     */
    void insertIfAbsent(@Param(Constants.ENTITY) BookFavoriteStats stats);

    /**
     * 原子增加一名收藏读者，et 参数用于填充修改审计字段。
     */
    int increase(@Param(Constants.ENTITY) BookFavoriteStats stats);

    /**
     * 在人数大于零时原子减一，et 参数用于填充修改审计字段。
     */
    int decrease(@Param(Constants.ENTITY) BookFavoriteStats stats);
}
