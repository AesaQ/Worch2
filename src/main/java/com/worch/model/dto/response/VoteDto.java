package com.worch.model.dto.response;

import com.worch.model.entity.Choice;
import com.worch.model.entity.ChoiceOption;
import com.worch.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VoteDto(
        @Schema(description = "Уникальный идентификатор голоса", required = true)
        UUID id,
        @Schema(description = "Идентификатор чойса", required = true)
        Choice choice,
        @Schema(description = "Идентификатор опции", required = true)
        ChoiceOption option,
        @Schema(description = "Идентификатор пользователя", required = true)
        User user,
        @Schema(description = "Время голосования", required = true)
        OffsetDateTime votedAt
) {}
