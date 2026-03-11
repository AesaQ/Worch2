package com.worch.mapper;

import com.worch.model.dto.request.CreateChoiceRequest;
import com.worch.model.dto.response.ChoiceDetailDto;
import com.worch.model.dto.response.ChoiceOptionDetailDto;
import com.worch.model.dto.response.ChoiceResponseDto;
import com.worch.model.entity.Choice;
import com.worch.model.entity.ChoiceOption;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    default ChoiceDetailDto toDetailDto(
            Choice choice,
            List<ChoiceOption> choiceOptions,
            @Context Map<UUID, Long> votesPerOptions,
            @Context Optional<UUID> votedOptionId
    ) {
        List<ChoiceOptionDetailDto> optionDtos = choiceOptions.stream()
                .map(option -> {
                    boolean votedByCurrentUser = votedOptionId
                            .map(option.getId()::equals)
                            .orElse(false);

                    long votes = votesPerOptions.getOrDefault(option.getId(), 0L);

                    return new ChoiceOptionDetailDto(
                            option.getId(),
                            option.getChoice().getId(),
                            option.getName(),
                            option.getPosition(),
                            votes,
                            votedByCurrentUser
                    );
                })
                .toList();

        return new ChoiceDetailDto(
                choice.getId(),
                choice.getCreator().getId(),
                choice.getChannel().getId(),
                choice.getTitle(),
                choice.getDescription(),
                choice.getImageLink(),
                choice.getPersonal(),
                choice.getStatus(),
                choice.getDeadline(),
                choice.getCreatedAt(),
                optionDtos
        );
    }
}
