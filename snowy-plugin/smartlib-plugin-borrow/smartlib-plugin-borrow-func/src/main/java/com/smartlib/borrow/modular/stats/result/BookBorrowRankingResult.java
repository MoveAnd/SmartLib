package com.smartlib.borrow.modular.stats.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "出版版本借阅排行榜条目")
public class BookBorrowRankingResult {
    @Schema(description = "排名，从1开始")
    private Integer rank;

    @Schema(description = "出版版本标识")
    private String editionKey;

    @Schema(description = "累计成功借阅次数")
    private Long borrowCount;
}
