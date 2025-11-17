package com.worch.mapper;

import com.worch.model.dto.response.ExpertProfileResponse;
import com.worch.model.entity.ExpertProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class ExpertProfileMapper {

    public ExpertProfileResponse toDto(ExpertProfile expertProfile) {
        return new ExpertProfileResponse(
                expertProfile.getId(),
                expertProfile.getUser().getId(),
                expertProfile.getIsIncognito(),
                expertProfile.getPrice(),
                expertProfile.getRating()
        );
    }
}