package com.worch.service;

import com.worch.model.dto.response.GroupDTO;
import com.worch.model.dto.response.GroupOwnerDto;
import com.worch.model.entity.Group;
import com.worch.model.entity.User;
import com.worch.repository.GroupRepository;
import com.worch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Setter
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public List<GroupDTO> getGroups() {
        List<Group> groups = groupRepository.findAll();

        if (groups.isEmpty()) {
            return Collections.emptyList();
        }

        Set<UUID> ownerIDs = groups.stream()
                .map(Group::getId)
                .collect(Collectors.toSet());

        Map<UUID, User> ownersMap = userRepository.findAllById(ownerIDs).stream().
                collect(Collectors.toMap(User::getId, Function.identity()));

        return groups.stream()
                .map(group -> toGroupDto(group, ownersMap))
                .collect(Collectors.toList());
    }

    private GroupDTO toGroupDto(Group group, Map<UUID, User> ownersMap) {
        User owner = ownersMap.get(group.getOwner().getId());

        if (owner == null) {
            throw new IllegalStateException("Владелец группы не найден: " + group.getId());
        }

        GroupOwnerDto ownerDto = new GroupOwnerDto(
                owner.getId(),
                getFullName(owner.getFirstName(), owner.getLastName())
        );

        return new GroupDTO(
                group.getId(),
                group.getName(),
                ownerDto,
                group.getCreatedAt()
        );
    }

    private String getFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
