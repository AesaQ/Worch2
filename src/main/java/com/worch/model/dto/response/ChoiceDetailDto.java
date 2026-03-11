package com.worch.model.dto.response;

import com.worch.model.enums.ChoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ChoiceDetailDto(
        @Schema(description = "Уникальный идентификатор выбора", required = true)
        UUID id,

        @Schema(description = "Идентификатор создателя", required = true)
        UUID creatorId,

        @Schema(description = "Идентификатор канала", required = true)
        UUID channelId,

        @Schema(description = "Название выбора", required = true)
        String title,

        @Schema(description = "Описание выбора", required = true)
        String description,

        @Schema(description = "Изображение в формате base64", required = true)
        String imageBase64,

        @Schema(description = "Личный выбор", required = true)
        boolean isPersonal,

        @Schema(description = "Статус выбора", required = true)
        ChoiceStatus status,

        @Schema(description = "Дедлайн выбора", required = true)
        OffsetDateTime deadline,

        @Schema(description = "Дата создания выбора", required = true)
        OffsetDateTime createdAt,

        @Schema(description = "Лист опций", required = true)
        List<ChoiceOptionDetailDto> choiceOptions
) {}
