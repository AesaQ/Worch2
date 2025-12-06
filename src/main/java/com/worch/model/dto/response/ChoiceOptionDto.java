package com.worch.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ChoiceOptionDto(
        @Schema(description = "Уникальный идентификатор опции", required = true)
        UUID id,

        @Schema(description = "Идентификатор выбора", required = true)
        UUID choiceId,

        @Schema(description = "Название опции", required = true)
        String name,

        @Schema(description = "Позиция опции", required = true)
        Integer position
) {}
