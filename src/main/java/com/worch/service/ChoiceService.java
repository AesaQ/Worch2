package com.worch.service;

import com.worch.exceptions.ChoiceNotFoundException;
import com.worch.exceptions.DuplicateVoteException;
import com.worch.mapper.ChoiceMapper;
import com.worch.model.dto.request.CreateChoiceRequest;
import com.worch.model.dto.request.UpdateChoiceRequest;
import com.worch.model.dto.request.VoteRequest;
import com.worch.model.entity.Choice;
import com.worch.model.enums.ChoiceStatus;
import com.worch.model.specification.ChoiceSpecifications;
import com.worch.repository.ChoiceRepository;
import com.worch.repository.ChoiceOptionRepository;

import java.time.OffsetDateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.worch.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.worch.model.entity.Vote;

import com.worch.model.entity.ChoiceOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ChoiceService {

    private final ChoiceRepository choiceRepository;
    private final ChoiceMapper choiceMapper;
    private final ChoiceOptionRepository choiceOptionRepository;
    private final UserService userService;
    private final ChannelService channelService;
    private final VoteRepository voteRepository;

    public Choice getChoice(UUID id) {
        return choiceRepository.findById(id)
                .orElseThrow(() -> new ChoiceNotFoundException("Choice not found: " + id));
    }

    public Page<Choice> getAllChoices(Optional<UUID> creatorId, Pageable pageable) {
        Specification<Choice> spec = ChoiceSpecifications.byCreatorId(creatorId.orElse(null));
        return choiceRepository.findAll(spec, pageable);
    }

    @Transactional
    public Choice createChoice(CreateChoiceRequest request) {
        Choice choice = choiceMapper.toEntity(request);
        return choiceRepository.save(choice);
    }

    public List<ChoiceOption> getChoiceOptionsOrElseThrow(UUID choiceId) {
        Choice choice = choiceRepository.findById(choiceId)
                .orElseThrow(() -> new ChoiceNotFoundException("Choice not found: " + choiceId));

        return choiceOptionRepository.findByChoiceId(choice.getId());
    }

    @Transactional
    public Choice updateChoice(UUID id, UpdateChoiceRequest request) {
        Choice existing = choiceRepository.findById(id)
                .orElseThrow(() -> new ChoiceNotFoundException("Choice not found: " + id));

        existing.setTitle(request.title());
        existing.setCreator(
                userService.getReferenceById(request.creatorId()));
        existing.setChannel(
                channelService.getReferenceById(request.channelId()));
        existing.setDescription(request.description());
        existing.setPersonal(request.isPersonal());
        existing.setStatus(ChoiceStatus.valueOf(String.valueOf(request.status())));
        existing.setDeadline(request.deadline());

        return choiceRepository.save(existing);
    }

    @Transactional
    public void deleteChoice(UUID uuid) {
        if (!choiceRepository.existsById(uuid)) {
            throw new ChoiceNotFoundException("Choice not found: " + uuid);
        }
        choiceRepository.deleteById(uuid);
    }

    @Transactional
    public Vote voteForChoice(UUID choiceId, VoteRequest voteRequest) {
        log.info("Processing vote for choice: {}, option: {}", choiceId, voteRequest.option());

        UUID userId = userService.getCurrentUserId();

        if (!choiceRepository.existsById(choiceId)) {
            throw new ChoiceNotFoundException("Choice not found: " + choiceId);
        }

        if (voteRepository.existsByChoiceIdAndUserId(choiceId, userId)) {
            throw new DuplicateVoteException("User has already voted for this choice: " + choiceId);
        }

        Vote vote = Vote.builder()
                .choice(getReferenceById(choiceId))
                .option(choiceOptionRepository.getReferenceById(voteRequest.option()))
                .user(userService.getReferenceById(userId)) // автоматически
                .votedAt(OffsetDateTime.now()) // автоматически
                .build();

        Vote savedVote = voteRepository.save(vote);

        log.info("Vote successfully recorded for user: {}", userId);

        return savedVote;

    }

    public Choice getReferenceById(UUID id) {
        return choiceRepository.getReferenceById(id);
    }
}