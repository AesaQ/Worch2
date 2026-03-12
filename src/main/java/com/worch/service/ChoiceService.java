package com.worch.service;

import com.worch.exceptions.*;
import com.worch.mapper.ChoiceMapper;
import com.worch.model.dto.response.ChoiceDetailDto;
import com.worch.model.entity.Choice;
import com.worch.model.enums.ChoiceStatus;
import com.worch.repository.ChoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.worch.model.dto.request.VoteRequest;
import com.worch.model.entity.ChoiceOption;
import com.worch.model.entity.User;
import com.worch.model.entity.Vote;
import com.worch.repository.ChoiceOptionRepository;
import com.worch.repository.VoteRepository;
import jakarta.persistence.EntityManager;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChoiceService {
    private final ChoiceRepository choiceRepository;
    private final ChoiceOptionRepository choiceOptionRepository;
    private final VoteRepository voteRepository;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final ChoiceMapper choiceMapper;

    @Transactional(readOnly = true)
    public List<Choice> getChoices(Optional<UUID> creatorId) {
        if (creatorId.isPresent()) {
            return choiceRepository.getAllByCreatorId(creatorId.get());
        }
        return choiceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ChoiceDetailDto getChoiceDetail(UUID choiceId) {
        Choice choice = choiceRepository.findById(choiceId)
                .orElseThrow(() -> new ChoiceOptionNotFoundException(choiceId.toString()));

        UUID currentUserId = currentUserService.getCurrentUserId();

        Optional<UUID> votedOptionId = voteRepository.findVotedOptionId(choiceId, currentUserId);

        List<ChoiceOption> choiceOptions = getChoiceOptions(choiceId);
        Map<UUID, Long> votesPerOptions = voteRepository
                .countVotesPerOption(choiceOptions.stream().map(ChoiceOption::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        r -> (UUID) r[0],
                        r -> (Long) r[1]
                ));

        return choiceMapper.toDetailDto(choice, choiceOptions, votesPerOptions, votedOptionId);
    }

    @Transactional
    public String closeChoice(UUID choiceId) {
        Choice choice = choiceRepository.findById(choiceId)
                .orElseThrow(() -> new ChoiceNotFoundException(choiceId.toString()));

        if (!choice.getCreator().getId().equals(currentUserService.getCurrentUserId())) {
            throw new AccessDeniedException("Закрывать чойс может только его создатель");
        }

        choice.setStatus(ChoiceStatus.CLOSED);
        choiceRepository.save(choice);
        return "Choice closed";
    }

    @Transactional
    public void vote(VoteRequest voteRequest, Jwt jwt) {
        Choice choice = choiceRepository.findById(UUID.fromString(voteRequest.choiceId()))
                .orElseThrow(() -> new ChoiceNotFoundException(voteRequest.choiceId()));

        ChoiceOption choiceOption = choiceOptionRepository.findById(UUID.fromString(voteRequest.choiceOptionId()))
                .orElseThrow(() -> new ChoiceOptionNotFoundException(voteRequest.choiceOptionId()));

        validateChoiceVote(choice, choiceOption);

        UUID currentUserId = UUID.fromString(jwt.getSubject());
        User userRef = entityManager.getReference(User.class, currentUserId);

        Vote vote = new Vote();
        vote.setChoice(choice);
        vote.setOption(choiceOption);
        vote.setUser(userRef);
        vote.setVotedAt(OffsetDateTime.now());

        voteRepository.save(vote);
    }

    private List<ChoiceOption> getChoiceOptions(UUID choiceId) {
        return choiceOptionRepository.findByChoiceId(choiceId);
    }

    private void validateChoiceVote(Choice choice, ChoiceOption choiceOption) {
        if (choice.getStatus().equals(ChoiceStatus.CLOSED)) {
            throw new ChoiceClosedException(choice.getId().toString());
        }

        if (choice.getDeadline() != null &&
                choice.getDeadline().isBefore(OffsetDateTime.now())) {
            throw new ChoiceExpiredException(choice.getId().toString());
        }

        if (!choiceOption.getChoice().getId().equals(choice.getId())) {
            throw new ChoiceOptionMismatchException(choice.getId(), choiceOption.getId());
        }

    }
}