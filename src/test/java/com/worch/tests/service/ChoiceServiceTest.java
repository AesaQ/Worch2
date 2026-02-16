package com.worch.tests.service;

import com.worch.exceptions.ChoiceNotFoundException;
import com.worch.exceptions.ChoiceOptionMismatchException;
import com.worch.exceptions.ChoiceOptionNotFoundException;
import com.worch.model.dto.request.VoteRequest;
import com.worch.model.entity.*;
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

    private static VoteRequest voteRequest;
    private static Choice choice;
    private static ChoiceOption choiceOption;

    @BeforeEach
    void setUp() {
        String choiceId = "7c9e3b5a-2f4d-4a6b-8c1e-9f2d3a5b7c9e";
        String choiceOptionId = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d";

        voteRequest = new VoteRequest(choiceId, choiceOptionId);

        choice = new Choice();
        choice.setId(UUID.fromString(choiceId));
        choice.setCreator(User.builder().login("user").build());
        choice.setChannel(Channel.builder().name("channel").build());
        choice.setTitle("title");
        choice.setImageLink("imageLink");
        choice.setPersonal(false);

        choiceOption = new ChoiceOption();
        choiceOption.setId(UUID.fromString(choiceOptionId));
        choiceOption.setChoice(choice);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void vote_success() {
        setupAuthentication();

        UUID choiceId = UUID.fromString(voteRequest.choiceId());
        UUID choiceOptionId = UUID.fromString(voteRequest.choiceOptionId());

        when(choiceRepository.findById(choiceId)).thenReturn(Optional.of(choice));
        when(choiceOptionRepository.findById(choiceOptionId)).thenReturn(Optional.of(choiceOption));

        choiceService.vote(voteRequest);

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

        assertThrows(ChoiceNotFoundException.class, () -> choiceService.vote(voteRequest));
    }

    @Test
    void vote_choiceOptionNotFound() {
        UUID choiceId = UUID.fromString(voteRequest.choiceId());
        UUID choiceOptionId = UUID.fromString(voteRequest.choiceOptionId());

        when(choiceRepository.findById(choiceId)).thenReturn(Optional.of(choice));
        when(choiceOptionRepository.findById(choiceOptionId)).thenReturn(Optional.empty());

        assertThrows(ChoiceOptionNotFoundException.class, () -> choiceService.vote(voteRequest));
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

        assertThrows(ChoiceOptionMismatchException.class, () -> choiceService.vote(voteRequest));
    }

    private void setupAuthentication() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId.toString());

        var auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);

        User userRef = User.builder().id(userId).login("user").build();
        when(entityManager.getReference(User.class, userId)).thenReturn(userRef);
    }
}