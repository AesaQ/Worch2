package com.worch.model.dto.response;

import com.worch.model.entity.User;

import java.util.UUID;

public record ExpertProfileDto(
        UUID id,
        User user,
        Boolean isIncognito,
        Integer price,
        Float rating
) {
}
