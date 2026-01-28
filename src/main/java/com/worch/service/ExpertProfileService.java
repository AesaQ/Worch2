package com.worch.service;

import com.worch.mapper.ExpertProfileMapper;
import com.worch.model.dto.response.ExpertProfileDto;
import com.worch.repository.ExpertProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpertProfileService {
    private final ExpertProfileRepository expertProfileRepository;
    private final ExpertProfileMapper expertProfileMapper;

    public List<ExpertProfileDto> getAllExpertProfiles() {
        return expertProfileRepository.findAll()
                .stream()
                .map(expertProfileMapper::toDto)
                .toList();
    }
}
