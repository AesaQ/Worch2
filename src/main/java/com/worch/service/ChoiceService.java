package com.worch.service;

import com.worch.model.dto.request.VoteRequest;
import com.worch.model.entity.Choice;
import com.worch.model.entity.ChoiceOption;
import com.worch.model.entity.User;
import com.worch.model.entity.Vote;
import com.worch.repository.ChoiceOptionRepository;
import com.worch.repository.ChoiceRepository;
import com.worch.repository.VoteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChoiceService {
    private final VoteRepository voteRepository;
    private final ChoiceRepository choiceRepository;
    private final ChoiceOptionRepository choiceOptionRepository;
    private final EntityManager entityManager;

    @Transactional
    public void vote(VoteRequest voteRequest) {
        Choice choice = choiceRepository.findById(UUID.fromString(voteRequest.choiceId()))
                .orElseThrow(() -> new EntityNotFoundException("Choice not found"));
        ChoiceOption choiceOption = choiceOptionRepository.findById(UUID.fromString(voteRequest.choiceOptionId()))
                .orElseThrow(() -> new EntityNotFoundException("ChoiceOption not found"));
        if (!choiceOption.getChoice().getId().equals(choice.getId())) {
            throw new RuntimeException("Choice id mismatch");
        }

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());
        User userRef = entityManager.getReference(User.class, userId);

        Vote vote = new Vote();

        vote.setChoice(choice);
        vote.setOption(choiceOption);
        vote.setUser(userRef);
        vote.setVotedAt(OffsetDateTime.now());

        voteRepository.save(vote);
    }
}