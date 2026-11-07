package com.smartlib.book.modular.favorite.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartlib.book.modular.favorite.entity.BookFavorite;
import com.smartlib.book.modular.favorite.param.BookFavoriteParam;

public interface BookFavoriteService extends IService<BookFavorite> {

    /**
     * 收藏出版版本，用户身份由 C 端登录上下文提供。
     */
    void add(BookFavoriteParam param, String userId);

    /**
     * 取消本人的收藏；未收藏或已取消时保持不变。
     */
    void cancel(BookFavoriteParam param, String userId);
}
