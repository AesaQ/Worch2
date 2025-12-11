package com.worch.mapper;

import com.worch.model.dto.response.ChoiceOptionDto;
import com.worch.model.entity.ChoiceOption;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChoiceOptionMapper {
    ChoiceOption toEntity(ChoiceOptionDto dto);
    ChoiceOptionDto toDto(ChoiceOption entity);
}
