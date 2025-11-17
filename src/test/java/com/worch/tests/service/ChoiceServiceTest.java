package com.worch.tests.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.worch.exceptions.ChoiceNotFoundException;
import com.worch.exceptions.DuplicateVoteException;
import com.worch.mapper.ChoiceMapper;
import com.worch.model.dto.request.CreateChoiceRequest;
import com.worch.model.dto.request.UpdateChoiceRequest;
import com.worch.model.dto.request.VoteRequest;
import com.worch.model.entity.Choice;
import com.worch.model.entity.ChoiceOption;
import com.worch.model.entity.User;
import com.worch.model.entity.Vote;
import com.worch.model.enums.ChoiceStatus;
import com.worch.repository.ChoiceRepository;
import com.worch.repository.VoteRepository;
import com.worch.service.UserService;

import com.worch.service.ChoiceService;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ChoiceServiceTest {

    @Mock
    private ChoiceRepository choiceRepository;

    @Mock
    private ChoiceMapper choiceMapper;

    @InjectMocks
    private ChoiceService choiceService;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private UserService userService;


    @Nested
    class CrudOperations {

        @Test
        void getChoice_ShouldReturnChoice_WhenExists() {
            UUID id = UUID.randomUUID();
            Choice choice = new Choice();
            choice.setId(id);

            when(choiceRepository.findById(id)).thenReturn(Optional.of(choice));

            Choice result = choiceService.getChoice(id);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            verify(choiceRepository).findById(id);
        }

        @Test
        void getChoice_ShouldThrow_WhenNotFound() {
            UUID id = UUID.randomUUID();
            when(choiceRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> choiceService.getChoice(id))
                    .isInstanceOf(ChoiceNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(choiceRepository).findById(id);
        }

        @Test
        void getAllChoices_ShouldReturnPagedResult() {
            UUID creatorId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            Choice choice = new Choice();
            List<Choice> choices = List.of(choice);
            Page<Choice> page = new PageImpl<>(choices, pageable, choices.size());

            when(choiceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            Page<Choice> result = choiceService.getAllChoices(Optional.of(creatorId), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(choiceRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        void createChoice_ShouldMapAndSave() {
            CreateChoiceRequest request = new CreateChoiceRequest(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "title",
                    "desc",
                    true,
                    ChoiceStatus.ACTIVE,
                    OffsetDateTime.now().plusDays(1)
            );

            Choice mappedChoice = new Choice();
            Choice savedChoice = new Choice();
            savedChoice.setId(UUID.randomUUID());

            when(choiceMapper.toEntity(request)).thenReturn(mappedChoice);
            when(choiceRepository.save(mappedChoice)).thenReturn(savedChoice);

            Choice result = choiceService.createChoice(request);

            assertThat(result).isEqualTo(savedChoice);
            verify(choiceMapper).toEntity(request);
            verify(choiceRepository).save(mappedChoice);
        }

        @Test
        void updateChoice_ShouldUpdateAndSave_WhenExists() {
            UUID id = UUID.randomUUID();

            UpdateChoiceRequest request = new UpdateChoiceRequest(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "updated title",
                    "updated desc",
                    true,
                    ChoiceStatus.HIDDEN,
                    OffsetDateTime.now().plusDays(2)
            );

            Choice existing = new Choice();
            existing.setId(id);
            existing.setStatus(ChoiceStatus.ACTIVE);

            when(choiceRepository.findById(id)).thenReturn(Optional.of(existing));
            when(choiceRepository.save(existing)).thenReturn(existing);

            Choice result = choiceService.updateChoice(id, request);

            assertThat(result.getTitle()).isEqualTo("updated title");
            assertThat(result.getStatus()).isEqualTo(ChoiceStatus.HIDDEN);
            verify(choiceRepository).findById(id);
            verify(choiceRepository).save(existing);
        }

        @Test
        void updateChoice_ShouldThrow_WhenNotFound() {
            UUID id = UUID.randomUUID();
            UpdateChoiceRequest request = mock(UpdateChoiceRequest.class);

            when(choiceRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> choiceService.updateChoice(id, request))
                    .isInstanceOf(ChoiceNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(choiceRepository).findById(id);
        }

        @Test
        void deleteChoice_ShouldDelete_WhenExists() {
            UUID id = UUID.randomUUID();
            when(choiceRepository.existsById(id)).thenReturn(true);
            doNothing().when(choiceRepository).deleteById(id);

            choiceService.deleteChoice(id);

            verify(choiceRepository).existsById(id);
            verify(choiceRepository).deleteById(id);
        }

        @Test
        void deleteChoice_ShouldThrow_WhenNotExists() {
            UUID id = UUID.randomUUID();
            when(choiceRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> choiceService.deleteChoice(id))
                    .isInstanceOf(ChoiceNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(choiceRepository).existsById(id);
            verify(choiceRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Тестирование операций голосования")
    class VotingOperations {

        private UUID choiceId;
        private UUID optionId;
        private UUID userId;
        private VoteRequest voteRequest;
        private Choice choice;
        private ChoiceOption option;
        private User user;

        @BeforeEach
        void setUp() {
            choiceId = UUID.randomUUID();
            optionId = UUID.randomUUID();
            userId = UUID.randomUUID();
            voteRequest = new VoteRequest(choiceId, optionId);
            choice = new Choice(choiceId,
                    null, null, null,
                    null, null, null,
                    null, null, null);
            option = new ChoiceOption(optionId, choice, null, null);
            user = new User(userId,
                    null, null, null,
                    null, null, null,
                    null, null, null);
        }

        @Test
        @DisplayName("Должен успешно создать голос и вернуть корректный результат")
        void voteForChoice_Success() {
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(choiceRepository.existsById(choiceId)).thenReturn(true);
            when(voteRepository.existsByChoiceIdAndUserId(choiceId, userId)).thenReturn(false);

            Vote savedVote = Vote.builder()
                    .id(UUID.randomUUID())
                    .choice(choice)
                    .option(option)
                    .user(user)
                    .votedAt(OffsetDateTime.now())
                    .build();

            when(voteRepository.save(any(Vote.class))).thenReturn(savedVote);

            Vote result = choiceService.voteForChoice(choiceId, voteRequest);

            assertThat(result).isNotNull();
            assertThat(result.getChoice().getId()).isEqualTo(choiceId);
            assertThat(result.getOption().getId()).isEqualTo(optionId);
            assertThat(result.getUser().getId()).isEqualTo(userId);
            assertThat(result.getVotedAt()).isNotNull();

            verify(userService).getCurrentUserId();
            verify(choiceRepository).existsById(choiceId);
            verify(voteRepository).existsByChoiceIdAndUserId(choiceId, userId);
            verify(voteRepository).save(any(Vote.class));
        }

        @Test
        @DisplayName("Должен выбрасывать исключение, если выбор не найден")
        void voteForChoice_ChoiceNotFound_ThrowsException() {
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(choiceRepository.existsById(choiceId)).thenReturn(false);

            assertThatThrownBy(() -> choiceService.voteForChoice(choiceId, voteRequest))
                    .isInstanceOf(ChoiceNotFoundException.class)
                    .hasMessageContaining("Choice not found: " + choiceId);

            verify(userService).getCurrentUserId();
            verify(choiceRepository).existsById(choiceId);
            verify(voteRepository, never()).existsByChoiceIdAndUserId(any(), any());
            verify(voteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при повторном голосовании")
        void voteForChoice_DuplicateVote_ThrowsException() {
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(choiceRepository.existsById(choiceId)).thenReturn(true);
            when(voteRepository.existsByChoiceIdAndUserId(choiceId, userId)).thenReturn(true);

            assertThatThrownBy(() -> choiceService.voteForChoice(choiceId, voteRequest))
                    .isInstanceOf(DuplicateVoteException.class)
                    .hasMessageContaining("already voted");

            verify(userService).getCurrentUserId();
            verify(choiceRepository).existsById(choiceId);
            verify(voteRepository).existsByChoiceIdAndUserId(choiceId, userId);
            verify(voteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Должен проверять корректность параметров создаваемого голоса")
        void voteForChoice_VerifyVoteCreation() {
            OffsetDateTime beforeVote = OffsetDateTime.now().minusSeconds(1);

            when(userService.getCurrentUserId()).thenReturn(userId);
            when(choiceRepository.existsById(choiceId)).thenReturn(true);
            when(voteRepository.existsByChoiceIdAndUserId(choiceId, userId)).thenReturn(false);
            when(voteRepository.save(any(Vote.class))).thenAnswer(invocation -> {
                Vote vote = invocation.getArgument(0);
                return Vote.builder()
                        .id(UUID.randomUUID())
                        .choice(vote.getChoice())
                        .option(vote.getOption())
                        .user(vote.getUser())
                        .votedAt(vote.getVotedAt())
                        .build();
            });

            Vote result = choiceService.voteForChoice(choiceId, voteRequest);

            verify(voteRepository).save(argThat(vote ->
                    vote.getChoice().getId().equals(choiceId) &&
                            vote.getOption().getId().equals(optionId) &&
                            vote.getUser().getId().equals(userId) &&
                            vote.getVotedAt() != null &&
                            vote.getVotedAt().isAfter(beforeVote)
            ));

            assertThat(result.getVotedAt()).isAfter(beforeVote);
        }
    }

}
