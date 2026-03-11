package com.worch.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ChoiceOptionDetailDto(
        @Schema(description = "Уникальный идентификатор опции", required = true)
        UUID id,

        @Schema(description = "Идентификатор выбора", required = true)
        UUID choiceId,

        @Schema(description = "Название опции", required = true)
        String name,

        @Schema(description = "Позиция опции", required = true)
        Integer position,

        @Schema(description = "Количество голосов", required = true)
        Long votes_count,

        @Schema(description = "Проголосовал ли текущий пользователь за эту опцию", required = true)
        Boolean votedByCurrentUser
) {}
