package com.smartlib.book.modular.favorite.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "出版版本收藏排行榜条目")
public class BookFavoriteRankingResult {

    @Schema(description = "排名，从1开始")
    private Integer rank;

    @Schema(description = "出版版本标识")
    private String editionKey;

    @Schema(description = "当前有效收藏人数")
    private Long favoriteCount;
}
