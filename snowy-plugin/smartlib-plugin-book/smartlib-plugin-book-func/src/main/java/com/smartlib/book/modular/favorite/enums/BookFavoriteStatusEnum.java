package com.smartlib.book.modular.favorite.enums;

import lombok.Getter;
import vip.xiaonuo.common.exception.CommonException;

/**
 * 收藏关系的业务状态，与逻辑删除标志分别维护。
 */
@Getter
public enum BookFavoriteStatusEnum {

    /**
     * 已收藏
     */
    FAVORITED("FAVORITED"),

    /**
     * 已取消收藏
     */
    CANCELLED("CANCELLED");

    private final String value;

    BookFavoriteStatusEnum(String value) {
        this.value = value;
    }

    public static void validate(String value) {
        for (BookFavoriteStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return;
            }
        }
        throw new CommonException("不支持的收藏状态：{}", value);
    }
}
