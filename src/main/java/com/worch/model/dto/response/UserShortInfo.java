package com.worch.model.dto.response;

import java.util.UUID;

public record UserShortInfo(
    UUID id,
    String name
) {
}
