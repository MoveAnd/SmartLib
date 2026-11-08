package com.smartlib.book.modular.edition.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.edition.param.BookEditionPageParam;
import com.smartlib.book.modular.edition.param.BookEditionEditParam;
import com.smartlib.book.modular.edition.result.BookEditionDetailResult;
import com.smartlib.book.modular.edition.result.BookEditionSummaryResult;

import java.util.List;

public interface BookEditionService {
    /**
     * 编辑现有出版版本的基础信息。
     */
    void edit(BookEditionEditParam param);

    Page<BookEditionSummaryResult> page(BookEditionPageParam param);

    BookEditionDetailResult detail(String key);

    List<BookEditionSummaryResult> byIsbn(String isbn);
}
