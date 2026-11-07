package com.smartlib.book.modular.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlib.book.modular.edition.entity.BookEdition;
import com.smartlib.book.modular.edition.mapper.BookEditionMapper;
import com.smartlib.book.modular.favorite.entity.BookFavorite;
import com.smartlib.book.modular.favorite.enums.BookFavoriteStatusEnum;
import com.smartlib.book.modular.favorite.mapper.BookFavoriteMapper;
import com.smartlib.book.modular.favorite.param.BookFavoriteParam;
import com.smartlib.book.modular.favorite.service.BookFavoriteService;
import com.smartlib.book.modular.favorite.service.BookFavoriteStatsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.common.exception.CommonException;

@Service
public class BookFavoriteServiceImpl extends ServiceImpl<BookFavoriteMapper, BookFavorite>
        implements BookFavoriteService {

    @Resource
    private BookEditionMapper bookEditionMapper;

    @Resource
    private BookFavoriteStatsService bookFavoriteStatsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(BookFavoriteParam param, String userId) {
        String editionKey = param.getEditionKey().trim();
        if (!bookEditionMapper.exists(new LambdaQueryWrapper<BookEdition>()
                .eq(BookEdition::getKey, editionKey))) {
            throw new CommonException("出版版本不存在，editionKey值为：{}", editionKey);
        }
        BookFavorite favorite = new BookFavorite();
        favorite.setUserId(userId);
        favorite.setEditionKey(editionKey);
        favorite.setStatus(BookFavoriteStatusEnum.FAVORITED.getValue());
        // 由唯一键原子处理并发创建，重复请求不改写已有记录。
        baseMapper.insertIfAbsent(favorite);
        // 本次分配的主键只有真正插入时才存在，不依赖 ODKU 的影响行数。
        boolean created = baseMapper.exists(new LambdaQueryWrapper<BookFavorite>()
                .eq(BookFavorite::getId, favorite.getId()));

        // 只恢复已取消的收藏，重复收藏不会刷新修改审计字段。
        BookFavorite restore = new BookFavorite();
        restore.setStatus(BookFavoriteStatusEnum.FAVORITED.getValue());
        boolean restored = this.update(restore, new LambdaUpdateWrapper<BookFavorite>()
                .eq(BookFavorite::getUserId, userId)
                .eq(BookFavorite::getEditionKey, editionKey)
                .eq(BookFavorite::getStatus, BookFavoriteStatusEnum.CANCELLED.getValue()));
        if (created || restored) {
            bookFavoriteStatsService.increase(editionKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(BookFavoriteParam param, String userId) {
        String editionKey = param.getEditionKey().trim();
        BookFavorite favorite = new BookFavorite();
        favorite.setStatus(BookFavoriteStatusEnum.CANCELLED.getValue());
        boolean cancelled = this.update(favorite, new LambdaUpdateWrapper<BookFavorite>()
                .eq(BookFavorite::getUserId, userId)
                .eq(BookFavorite::getEditionKey, editionKey)
                .eq(BookFavorite::getStatus, BookFavoriteStatusEnum.FAVORITED.getValue()));
        if (cancelled) {
            bookFavoriteStatsService.decrease(editionKey);
        }
    }
}
