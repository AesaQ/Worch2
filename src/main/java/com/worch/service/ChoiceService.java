package com.worch.service;

import com.worch.model.entity.Choice;
import com.worch.repository.ChoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChoiceService {
    private final ChoiceRepository choiceRepository;

    @Transactional(readOnly = true)
    public List<Choice> getChoices(Optional<UUID> creatorId) {
        if (creatorId.isPresent()) {
            return choiceRepository.getAllByCreatorId(creatorId.get());
        }
        return choiceRepository.findAll();
    }
}