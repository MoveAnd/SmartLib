package com.smartlib.book.modular.author.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.author.param.BookAuthorPageParam;
import com.smartlib.book.modular.author.result.BookAuthorDetailResult;
import com.smartlib.book.modular.author.result.BookAuthorSummaryResult;

public interface BookAuthorService {
    Page<BookAuthorSummaryResult> page(BookAuthorPageParam param);

    BookAuthorDetailResult detail(String key);
}
