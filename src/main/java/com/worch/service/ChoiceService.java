package com.worch.service;

import com.worch.exceptions.*;
import com.worch.model.entity.Choice;
import com.worch.repository.ChoiceRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.worch.model.dto.request.VoteRequest;
import com.worch.model.entity.ChoiceOption;
import com.worch.model.entity.User;
import com.worch.model.entity.Vote;
import com.worch.repository.ChoiceOptionRepository;
import com.worch.repository.VoteRepository;
import jakarta.persistence.EntityManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChoiceService {
    private final ChoiceRepository choiceRepository;
    private final ChoiceOptionRepository choiceOptionRepository;
    private final VoteRepository voteRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Choice> getChoices(Optional<UUID> creatorId) {
        if (creatorId.isPresent()) {
            return choiceRepository.getAllByCreatorId(creatorId.get());
        }
        return choiceRepository.findAll();
    }
    
    @Transactional
    public String vote(VoteRequest voteRequest) {
        Choice choice = choiceRepository.findById(UUID.fromString(voteRequest.choiceId()))
                .orElseThrow(() -> new ChoiceNotFoundException(voteRequest.choiceId()));

        ChoiceOption choiceOption = choiceOptionRepository.findById(UUID.fromString(voteRequest.choiceOptionId()))
                .orElseThrow(() -> new ChoiceOptionNotFoundException(voteRequest.choiceOptionId()));

        if (!choiceOption.getChoice().getId().equals(choice.getId())) {
            throw new ChoiceOptionMismatchException(choice.getId(), choiceOption.getId());
        }

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(jwt.getSubject());
        User userRef = entityManager.getReference(User.class, userId);

        Vote vote = new Vote();

        vote.setChoice(choice);
        vote.setOption(choiceOption);
        vote.setUser(userRef);
        vote.setVotedAt(OffsetDateTime.now());

        try {
            voteRepository.save(vote);
            voteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException cve &&
                    "ux_vote_user_choice".equals(cve.getConstraintName())) {

                throw new DuplicateVoteException(e.getMessage());
            }
            throw e;
        }
        return "{\"message\":\"Vote accepted\"}";
    }
}