package com.worch.model.dto.request;

import com.worch.model.enums.ChoiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateChoiceRequest(
    @NotNull(message = "Поле 'creatorId' не должно быть null")
    UUID creatorId,

    @NotNull(message = "Поле 'channelId' не должно быть null")
    UUID channelId,

    @NotBlank(message = "Поле 'channelId' обязательно для заполнения") @Size(max = 300)
    String title,

    String description,

    boolean isPersonal,

    @NotNull(message = "Поле 'status' не должно быть null")
    ChoiceStatus status,

    @NotNull(message = "Поле 'deadline' не должно быть null")
    OffsetDateTime deadline
) {

}
