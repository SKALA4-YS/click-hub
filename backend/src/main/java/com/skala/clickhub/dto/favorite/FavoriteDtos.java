package com.skala.clickhub.dto.favorite;

public final class FavoriteDtos {

    private FavoriteDtos() {}

    public record FavoriteResponse(
            boolean favorited
    ) {}
}
