package com.smartlib.book.modular.favorite.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

/**
 * 读者与出版版本的收藏关系。
 */
@Getter
@Setter
@TableName("book_favorite")
public class BookFavorite extends CommonEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏记录ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * C端读者ID
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 出版版本标识，逻辑关联 editions.key
     */
    @TableField(value = "edition_key")
    private String editionKey;

    /**
     * 收藏状态：FAVORITED已收藏，CANCELLED已取消
     */
    @TableField(value = "status")
    private String status;
}
