package com.smartlib.book.modular.favorite.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlib.book.modular.favorite.entity.BookFavoriteStats;
import com.smartlib.book.modular.favorite.mapper.BookFavoriteStatsMapper;
import com.smartlib.book.modular.favorite.service.BookFavoriteStatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.common.exception.CommonException;

@Service
public class BookFavoriteStatsServiceImpl extends ServiceImpl<BookFavoriteStatsMapper, BookFavoriteStats>
        implements BookFavoriteStatsService {

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
