package com.worch.mapper;

import com.worch.model.dto.response.ExpertProfileDto;
import com.worch.model.dto.response.UserShortInfo;
import com.worch.model.entity.ExpertProfile;
import com.worch.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpertProfileMapper {
    ExpertProfile toEntity(ExpertProfileDto dto);
    ExpertProfileDto toDto(ExpertProfile expertProfile);

    default UserShortInfo mapUserToShortInfo(User user) {
        return new UserShortInfo(user.getId(), user.getFirstName());
    }
}
