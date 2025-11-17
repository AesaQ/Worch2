package com.worch.mapper;

import com.worch.model.dto.response.ChoiceOptionStatsDto;
import com.worch.model.entity.ChoiceOption;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class ChoiceOptionMapper {
    public ChoiceOptionStatsDto toStatsDto(ChoiceOption option, long votes, boolean isMyOption) {
        return new ChoiceOptionStatsDto(
                option.getId(),
                option.getName(),
                option.getPosition(),
                votes,
                isMyOption
        );
    }
}
