package com.worch.mapper;

import com.worch.model.dto.request.CreateChoiceRequest;
import com.worch.model.dto.response.ChoiceResponseDto;
import com.worch.model.entity.Choice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ChoiceMapper {

    @Mappings({
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "personal", source = "isPersonal")
    })
    Choice toEntity(CreateChoiceRequest request);

    @Mappings({
            @Mapping(target = "isPersonal", source = "personal"),
            @Mapping(target = "status", source = "status", resultType = String.class),
            @Mapping(target = "imageBase64", ignore = true),
    })
    ChoiceResponseDto toDto(Choice choice);
}
