package com.worch.tests.service;

import com.worch.exceptions.*;
import com.worch.model.dto.request.VoteRequest;
import com.worch.model.entity.*;
import com.worch.model.enums.ChoiceStatus;
import com.worch.repository.ChoiceOptionRepository;
import com.worch.repository.ChoiceRepository;
import com.worch.repository.VoteRepository;
import com.worch.service.ChoiceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Тест сервиса чойсов")
@ExtendWith(MockitoExtension.class)
public class ChoiceServiceTest {
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private ChoiceRepository choiceRepository;
    @Mock
    private ChoiceOptionRepository choiceOptionRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ChoiceService choiceService;

    private VoteRequest voteRequest;
    private Choice choice;
    private ChoiceOption choiceOption;
    private String userId;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        String choiceId = "7c9e3b5a-2f4d-4a6b-8c1e-9f2d3a5b7c9e";
        String choiceOptionId = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d";
        userId = "b3f8c2a1-6d4e-4c9a-9f7b-2e5d8a1c6f3b";

        voteRequest = new VoteRequest(choiceId, choiceOptionId);

        choice = new Choice();
        choice.setId(UUID.fromString(choiceId));
        choice.setCreator(User.builder().login("user").build());
        choice.setChannel(Channel.builder().name("channel").build());
        choice.setTitle("title");
        choice.setImageLink("imageLink");
        choice.setPersonal(false);
        choice.setStatus(ChoiceStatus.ACTIVE);
        choice.setDeadline(OffsetDateTime.now().plusHours(1));

        choiceOption = new ChoiceOption();
        choiceOption.setId(UUID.fromString(choiceOptionId));
        choiceOption.setChoice(choice);

        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void vote_success() {
        UUID choiceId = UUID.fromString(voteRequest.choiceId());
        UUID choiceOptionId = UUID.fromString(voteRequest.choiceOptionId());

        when(choiceRepository.findById(choiceId)).thenReturn(Optional.of(choice));
        when(choiceOptionRepository.findById(choiceOptionId)).thenReturn(Optional.of(choiceOption));

        choiceService.vote(voteRequest, jwt);

        ArgumentCaptor<Vote> voteCaptor = ArgumentCaptor.forClass(Vote.class);
        verify(voteRepository).save(voteCaptor.capture());

        Vote vote = voteCaptor.getValue();

        assertAll(
                () -> assertNotNull(vote),
                () -> assertEquals(choice, vote.getChoice()),
                () -> assertEquals(choiceOption, vote.getOption()),
                () -> assertNotNull(vote.getVotedAt())
        );
    }

    @Test
    void vote_choiceNotFound() {
        UUID choiceId = UUID.fromString(voteRequest.choiceId());

        when(choiceRepository.findById(choiceId)).thenReturn(Optional.empty());

        assertThrows(ChoiceNotFoundException.class, () -> choiceService.vote(voteRequest, jwt));
    }

    @Test
    void vote_choiceOptionNotFound() {
        UUID choiceId = UUID.fromString(voteRequest.choiceId());
        UUID choiceOptionId = UUID.fromString(voteRequest.choiceOptionId());

        when(choiceRepository.findById(choiceId)).thenReturn(Optional.of(choice));
        when(choiceOptionRepository.findById(choiceOptionId)).thenReturn(Optional.empty());

        assertThrows(ChoiceOptionNotFoundException.class, () -> choiceService.vote(voteRequest, jwt));
    }

    @Test
    void vote_choiceOptionMismatch() {
        UUID choiceId = UUID.fromString(voteRequest.choiceId());
        UUID choiceOptionId = UUID.fromString(voteRequest.choiceOptionId());

        Choice anotherChoice = new Choice();
        anotherChoice.setId(UUID.randomUUID());

        choiceOption.setChoice(anotherChoice);

        when(choiceRepository.findById(choiceId)).thenReturn(Optional.of(choice));
        when(choiceOptionRepository.findById(choiceOptionId)).thenReturn(Optional.of(choiceOption));

        assertThrows(ChoiceOptionMismatchException.class, () -> choiceService.vote(voteRequest, jwt));
    }

    @Test
    void vote_choiceClosed() {
        UUID choiceId = UUID.fromString(voteRequest.choiceId());
        UUID choiceOptionId = UUID.fromString(voteRequest.choiceOptionId());

        choice.setStatus(ChoiceStatus.CLOSED);
        choice.setDeadline(OffsetDateTime.now().plusHours(1));

        when(choiceRepository.findById(choiceId)).thenReturn(Optional.of(choice));
        when(choiceOptionRepository.findById(choiceOptionId)).thenReturn(Optional.of(choiceOption));

        assertThrows(ChoiceClosedException.class, () -> choiceService.vote(voteRequest, jwt));

        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void vote_choiceExpired() {
        UUID choiceId = UUID.fromString(voteRequest.choiceId());
        UUID choiceOptionId = UUID.fromString(voteRequest.choiceOptionId());

        choice.setStatus(ChoiceStatus.ACTIVE);
        choice.setDeadline(OffsetDateTime.now().minusMinutes(1));

        when(choiceRepository.findById(choiceId)).thenReturn(Optional.of(choice));
        when(choiceOptionRepository.findById(choiceOptionId)).thenReturn(Optional.of(choiceOption));

        assertThrows(ChoiceExpiredException.class, () -> choiceService.vote(voteRequest, jwt));

        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void vote_choiceActiveAndNotExpired_success() {
        UUID choiceId = UUID.fromString(voteRequest.choiceId());
        UUID choiceOptionId = UUID.fromString(voteRequest.choiceOptionId());

        choice.setStatus(ChoiceStatus.ACTIVE);
        choice.setDeadline(OffsetDateTime.now().plusHours(2));

        when(choiceRepository.findById(choiceId)).thenReturn(Optional.of(choice));
        when(choiceOptionRepository.findById(choiceOptionId)).thenReturn(Optional.of(choiceOption));

        choiceService.vote(voteRequest, jwt);

        ArgumentCaptor<Vote> voteCaptor = ArgumentCaptor.forClass(Vote.class);
        verify(voteRepository).save(voteCaptor.capture());

        Vote vote = voteCaptor.getValue();

        assertAll(
                () -> assertNotNull(vote),
                () -> assertEquals(choice, vote.getChoice()),
                () -> assertEquals(choiceOption, vote.getOption()),
                () -> assertNotNull(vote.getVotedAt())
        );
    }

    @Test
    void vote_choiceWithoutDeadline_success() {
        UUID choiceId = UUID.fromString(voteRequest.choiceId());
        UUID choiceOptionId = UUID.fromString(voteRequest.choiceOptionId());

        choice.setStatus(ChoiceStatus.ACTIVE);
        choice.setDeadline(null);

        when(choiceRepository.findById(choiceId)).thenReturn(Optional.of(choice));
        when(choiceOptionRepository.findById(choiceOptionId)).thenReturn(Optional.of(choiceOption));

        choiceService.vote(voteRequest, jwt);

        verify(voteRepository).save(any(Vote.class));
    }

    @Test
    void getChoices_filterByCreatorIdOnly_success() {
        UUID creatorId = UUID.randomUUID();

        Choice choice1 = new Choice();
        choice1.setId(UUID.randomUUID());
        choice1.setStatus(ChoiceStatus.ACTIVE);

        Choice choice2 = new Choice();
        choice2.setId(UUID.randomUUID());
        choice2.setStatus(ChoiceStatus.CLOSED);

        when(choiceRepository.getAllByCreatorId(creatorId))
                .thenReturn(List.of(choice1, choice2));

        List<Choice> result = choiceService.getChoices(Optional.of(creatorId), Optional.empty());

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(List.of(choice1, choice2), result)
        );

        verify(choiceRepository).getAllByCreatorId(creatorId);
        verify(choiceRepository, never()).findByStatus(any());
        verify(choiceRepository, never()).getAllByCreatorIdAndStatus(any(), any());
        verify(choiceRepository, never()).findAll();
    }

    @Test
    void getChoices_filterByStatusOnly_success() {
        Choice activeChoice1 = new Choice();
        activeChoice1.setId(UUID.randomUUID());
        activeChoice1.setStatus(ChoiceStatus.ACTIVE);

        Choice activeChoice2 = new Choice();
        activeChoice2.setId(UUID.randomUUID());
        activeChoice2.setStatus(ChoiceStatus.ACTIVE);

        when(choiceRepository.findByStatus(ChoiceStatus.ACTIVE))
                .thenReturn(List.of(activeChoice1, activeChoice2));

        List<Choice> result = choiceService.getChoices(Optional.empty(), Optional.of("ACTIVE"));

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().allMatch(choice -> choice.getStatus().equals(ChoiceStatus.ACTIVE))),
                () -> assertEquals(List.of(activeChoice1, activeChoice2), result)
        );

        verify(choiceRepository).findByStatus(ChoiceStatus.ACTIVE);
        verify(choiceRepository, never()).getAllByCreatorId(any());
        verify(choiceRepository, never()).getAllByCreatorIdAndStatus(any(), any());
        verify(choiceRepository, never()).findAll();
    }

    @Test
    void getChoices_filterByCreatorIdAndStatus_success() {
        UUID creatorId = UUID.randomUUID();

        Choice activeChoice = new Choice();
        activeChoice.setId(UUID.randomUUID());
        activeChoice.setStatus(ChoiceStatus.ACTIVE);

        when(choiceRepository.getAllByCreatorIdAndStatus(creatorId, ChoiceStatus.ACTIVE))
                .thenReturn(List.of(activeChoice));

        List<Choice> result = choiceService.getChoices(Optional.of(creatorId), Optional.of("ACTIVE"));

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(ChoiceStatus.ACTIVE, result.get(0).getStatus()),
                () -> assertEquals(List.of(activeChoice), result)
        );

        verify(choiceRepository).getAllByCreatorIdAndStatus(creatorId, ChoiceStatus.ACTIVE);
        verify(choiceRepository, never()).getAllByCreatorId(any());
        verify(choiceRepository, never()).findByStatus(any());
        verify(choiceRepository, never()).findAll();
    }

    @Test
    void getChoices_withoutFilters_returnsAllChoices() {
        Choice choice1 = new Choice();
        choice1.setId(UUID.randomUUID());
        choice1.setStatus(ChoiceStatus.ACTIVE);

        Choice choice2 = new Choice();
        choice2.setId(UUID.randomUUID());
        choice2.setStatus(ChoiceStatus.CLOSED);

        when(choiceRepository.findAll())
                .thenReturn(List.of(choice1, choice2));

        List<Choice> result = choiceService.getChoices(Optional.empty(), Optional.empty());

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(List.of(choice1, choice2), result)
        );

        verify(choiceRepository).findAll();
        verify(choiceRepository, never()).getAllByCreatorId(any());
        verify(choiceRepository, never()).findByStatus(any());
        verify(choiceRepository, never()).getAllByCreatorIdAndStatus(any(), any());
    }

    @Test
    void getChoices_filterByStatusOnly_emptyResult() {
        when(choiceRepository.findByStatus(ChoiceStatus.CLOSED))
                .thenReturn(List.of());

        List<Choice> result = choiceService.getChoices(Optional.empty(), Optional.of("CLOSED"));

        assertTrue(result.isEmpty());

        verify(choiceRepository).findByStatus(ChoiceStatus.CLOSED);
        verify(choiceRepository, never()).getAllByCreatorId(any());
        verify(choiceRepository, never()).getAllByCreatorIdAndStatus(any(), any());
        verify(choiceRepository, never()).findAll();
    }

    @Test
    void getChoices_filterByCreatorIdAndStatus_emptyResult() {
        UUID creatorId = UUID.randomUUID();

        when(choiceRepository.getAllByCreatorIdAndStatus(creatorId, ChoiceStatus.CLOSED))
                .thenReturn(List.of());

        List<Choice> result = choiceService.getChoices(Optional.of(creatorId), Optional.of("CLOSED"));

        assertTrue(result.isEmpty());

        verify(choiceRepository).getAllByCreatorIdAndStatus(creatorId, ChoiceStatus.CLOSED);
        verify(choiceRepository, never()).getAllByCreatorId(any());
        verify(choiceRepository, never()).findByStatus(any());
        verify(choiceRepository, never()).findAll();
    }

    @Test
    void getChoices_invalidStatus_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> choiceService.getChoices(Optional.empty(), Optional.of("INVALID_STATUS")));

        verify(choiceRepository, never()).getAllByCreatorId(any());
        verify(choiceRepository, never()).findByStatus(any());
        verify(choiceRepository, never()).getAllByCreatorIdAndStatus(any(), any());
        verify(choiceRepository, never()).findAll();
    }
}