package com.smartlib.book.core.job;

import com.smartlib.book.modular.favorite.job.BookFavoriteRankingRefreshJob;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookFavoriteRankingJobHandler {

    private final BookFavoriteRankingRefreshJob bookFavoriteRankingRefreshJob;

    @XxlJob("refreshBookFavoriteRanking")
    public void refreshBookFavoriteRanking() {
        bookFavoriteRankingRefreshJob.refresh();
    }
}
