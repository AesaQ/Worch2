package com.worch.model.dto.response;

import com.worch.model.entity.Choice;
import com.worch.model.entity.ChoiceOption;
import com.worch.model.entity.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VoteDto(
        UUID id,
        Choice choice,
        ChoiceOption option,
        User user,
        OffsetDateTime votedAt
) {}
