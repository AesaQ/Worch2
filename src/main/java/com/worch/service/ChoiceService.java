package com.worch.service;

import com.worch.model.entity.Choice;
import com.worch.repository.ChoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChoiceService {
    private final ChoiceRepository choiceRepository;

    @Transactional(readOnly = true)
    public List<Choice> getAllChoices() {
        return choiceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Choice> getAllChoicesByCreatorId(UUID creatorId) {
        return choiceRepository.getAllByCreatorId(creatorId);
    }

}
