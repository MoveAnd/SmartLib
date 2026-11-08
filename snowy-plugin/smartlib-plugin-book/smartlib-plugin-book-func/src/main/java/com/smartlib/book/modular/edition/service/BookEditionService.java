package com.smartlib.book.modular.edition.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.edition.param.BookEditionDeleteParam;
import com.smartlib.book.modular.edition.param.BookEditionEditParam;
import com.smartlib.book.modular.edition.param.BookEditionPageParam;
import com.smartlib.book.modular.edition.result.BookEditionDetailResult;
import com.smartlib.book.modular.edition.result.BookEditionSummaryResult;

import java.util.List;

public interface BookEditionService {
    /**
     * 编辑现有出版版本的基础信息。
     */
    void edit(BookEditionEditParam param);

    /** 逻辑删除出版版本；存在馆藏或有效收藏时拒绝，不存在或已删除时视为成功。 */
    void delete(BookEditionDeleteParam param);

    Page<BookEditionSummaryResult> page(BookEditionPageParam param);

    BookEditionDetailResult detail(String key);

    List<BookEditionSummaryResult> byIsbn(String isbn);
}
