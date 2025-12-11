package com.worch.mapper;

import com.worch.model.dto.response.VoteDto;
import com.worch.model.entity.Vote;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VoteMapper {
    Vote toEntity(VoteDto dto);
    VoteDto toDto(Vote entity);
}
