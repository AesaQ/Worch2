package com.worch.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ExpertProfileDto(
        @Schema(description = "Уникальный идентификатор профиля эксперта", required = true)
        UUID id,
        @Schema(description = "Идентификатор пользователя", required = true)
        UUID userId,
        @Schema(description = "Имя пользователя", required = true)
        String username,
        @Schema(description = "Является ли профиль инкогнито?", required = true)
        Boolean isIncognito,
        @Schema(description = "Цена", required = true)
        Integer price,
        @Schema(description = "Рейтинг", required = true)
        Float rating
) {
}
