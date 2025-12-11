package com.worch.model.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChannelResponseDto(
        UUID id ,
        String name ,
        String description ,
        boolean is  ,
        String password ,
        boolean ageRestricted ,
        UUID ownerId ,
        OffsetDateTime createdAt
) {
}
