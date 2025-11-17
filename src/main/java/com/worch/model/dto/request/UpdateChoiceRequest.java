package com.worch.model.dto.request;

import com.worch.model.enums.ChoiceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateChoiceRequest(
    UUID creatorId,
    UUID channelId,
    @Size(max = 300) String title,
    String description,
    //byte[] image,
    boolean isPersonal,
    @NotNull ChoiceStatus status,
    OffsetDateTime deadline
) {

}
