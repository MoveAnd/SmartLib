package com.smartlib.book.modular.favorite.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

/**
 * 按出版版本统计的当前有效收藏人数。
 */
@Getter
@Setter
@TableName("book_favorite_stats")
public class BookFavoriteStats extends CommonEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 出版版本标识，逻辑关联 editions.key
     */
    @TableId(value = "edition_key", type = IdType.INPUT)
    private String editionKey;

    /**
     * 当前有效收藏人数
     */
    @TableField(value = "favorite_count")
    private Long favoriteCount;
}
