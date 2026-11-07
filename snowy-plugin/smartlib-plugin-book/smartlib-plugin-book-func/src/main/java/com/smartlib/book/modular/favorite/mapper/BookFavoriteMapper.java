package com.smartlib.book.modular.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.smartlib.book.modular.favorite.entity.BookFavorite;
import org.apache.ibatis.annotations.Param;

/**
 * book_favorite 基础持久化接口。
 */
public interface BookFavoriteMapper extends BaseMapper<BookFavorite> {

    /**
     * 创建缺失的收藏关系，已有记录及审计字段保持不变。
     * 参数必须保留 Constants.ENTITY（et），MyBatis-Plus 据此生成主键并自动填充公共审计字段。
     */
    void insertIfAbsent(@Param(Constants.ENTITY) BookFavorite favorite);
}
