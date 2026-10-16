package com.smartlib.book.modular.work.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.work.param.BookWorkPageParam;
import com.smartlib.book.modular.work.result.BookWorkDetailResult;
import com.smartlib.book.modular.work.result.BookWorkSummaryResult;

public interface BookWorkService {
    Page<BookWorkSummaryResult> page(BookWorkPageParam param);

    BookWorkDetailResult detail(String key);
}
