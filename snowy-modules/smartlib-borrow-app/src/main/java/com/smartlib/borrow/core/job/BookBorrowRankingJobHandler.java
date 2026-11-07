package com.smartlib.borrow.core.job;

import com.smartlib.borrow.modular.stats.job.BookBorrowRankingRefreshJob;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookBorrowRankingJobHandler {

    private final BookBorrowRankingRefreshJob bookBorrowRankingRefreshJob;

    @XxlJob("refreshBookBorrowRanking")
    public void refreshBookBorrowRanking() {
        bookBorrowRankingRefreshJob.refresh();
    }
}
