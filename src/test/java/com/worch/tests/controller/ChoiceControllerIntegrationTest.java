package com.worch.tests.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.worch.model.dto.request.CreateChoiceRequest;
import com.worch.model.dto.request.UpdateChoiceRequest;
import com.worch.model.dto.request.VoteRequest;
import com.worch.model.entity.Channel;
import com.worch.model.entity.Choice;
import com.worch.model.entity.User;
import com.worch.model.enums.ChoiceStatus;
import com.worch.repository.ChoiceRepository;
import com.worch.repository.UserRepository;
import com.worch.repository.VoteRepository;

import com.worch.tests.common.AbstractIntegrationTest;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChoiceControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoteRepository voteRepository;

    private UUID choiceId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        choiceRepository.deleteAll();
        userRepository.deleteAll();

        User testUser = User.builder()
                .id(UUID.randomUUID())
                .login("testuser1")
                .password("password")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();

        Choice choice = new Choice();
        choice.setId(UUID.randomUUID());
        choice.setTitle("Initial title");
        choice.setDescription("Initial description");
        choice.setCreator(testUser);
        choice.setChannel(new Channel());
        choice.setPersonal(false);
        choice.setStatus(ChoiceStatus.ACTIVE);
        choice.setDeadline(OffsetDateTime.now().plusDays(5));
        choice = choiceRepository.save(choice);
        choiceId = choice.getId();

    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CrudOperations {

        @Test
        @Order(1)
        void createChoice_shouldReturnCreatedChoice() throws Exception {
            CreateChoiceRequest request = new CreateChoiceRequest(
                    testUserId,
                    UUID.randomUUID(),
                    "Test title",
                    "Test desc",
                    true,
                    ChoiceStatus.ACTIVE,
                    OffsetDateTime.now().plusDays(1)
            );

            mockMvc.perform(post("/api/v1/choices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Test title"));
        }

        @Test
        @Order(2)
        void getChoice_shouldReturnChoice() throws Exception {
            mockMvc.perform(get("/api/v1/choices/{id}", choiceId)
                            .param("userId", testUserId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }


        @Test
        @Order(3)
        void updateChoice_shouldUpdateAndReturnChoice() throws Exception {
            UpdateChoiceRequest request = new UpdateChoiceRequest(
                    testUserId,
                    UUID.randomUUID(),
                    "Updated title",
                    "Updated desc",
                    true,
                    ChoiceStatus.HIDDEN,
                    OffsetDateTime.now().plusDays(10)
            );

            mockMvc.perform(put("/api/v1/choices/{id}", choiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated title"));
        }

        @Test
        @Order(4)
        void deleteChoice_shouldRemoveChoice() throws Exception {
            mockMvc.perform(delete("/api/v1/choices/{id}", choiceId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @Order(5)
        void createChoice_shouldFailValidation() throws Exception {
            CreateChoiceRequest invalidRequest = new CreateChoiceRequest(
                    null,
                    null,
                    "",
                    "",
                    false,
                    ChoiceStatus.ACTIVE,
                    null
            );

            mockMvc.perform(post("/api/v1/choices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Тестирование операций голосования")
    class VotingOperations {

        @Test
        @WithMockUser(username = "testuser1")
        @DisplayName("Должен успешно создать голос и вернуть 201 с данными голоса")
        void voteForChoice_Success() throws Exception {
            UUID optionId = UUID.randomUUID();
            VoteRequest voteRequest = new VoteRequest(choiceId, optionId);

            mockMvc.perform(post("/api/v1/choices/{id}/vote", choiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(voteRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpectAll(
                            jsonPath("$.voteId").exists(),
                            jsonPath("$.voteId").isNotEmpty(),
                            jsonPath("$.choiceId").value(choiceId.toString()),
                            jsonPath("$.optionId").value(optionId.toString()),
                            jsonPath("$.userId").value(testUserId.toString()),
                            jsonPath("$.votedAt").exists()
                    );

            long voteCount = voteRepository.count();
            assertThat(voteCount).isEqualTo(1);
            assertThat(voteRepository.existsByChoiceIdAndUserId(choiceId, testUserId)).isTrue();
        }

        @Test
        @WithMockUser(username = "testuser1")
        @DisplayName("Должен вернуть 409 Conflict при попытке повторного голосования")
        void voteForChoice_DuplicateVote_Returns409() throws Exception {
            UUID optionId = UUID.randomUUID();
            VoteRequest voteRequest = new VoteRequest(choiceId, optionId);

            mockMvc.perform(post("/api/v1/choices/{id}/vote", choiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(voteRequest)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/choices/{id}/vote", choiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(voteRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpectAll(
                            jsonPath("$.message").value("Вы уже проголосовали за этот выбор"),
                            jsonPath("$.timestamp").exists()
                    );

            long voteCount = voteRepository.count();
            assertThat(voteCount).isEqualTo(1);
        }

        @Test
        @WithMockUser(username = "testuser1")
        @DisplayName("Должен вернуть 404 когда выбор не существует")
        void voteForChoice_ChoiceNotFound_Returns404() throws Exception {
            UUID nonExistentChoiceId = UUID.randomUUID();
            UUID optionId = UUID.randomUUID();
            VoteRequest voteRequest = new VoteRequest(nonExistentChoiceId, optionId);

            mockMvc.perform(post("/api/v1/choices/{id}/vote", nonExistentChoiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(voteRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            long voteCount = voteRepository.count();
            assertThat(voteCount).isZero();
        }

        @Test
        @WithMockUser(username = "testuser1")
        @DisplayName("Должен вернуть 400 при невалидном запросе")
        void voteForChoice_InvalidRequest_Returns400() throws Exception {
            mockMvc.perform(post("/api/v1/choices/{id}/vote", choiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"choice\": null, \"option\": null}"))
                    .andExpect(status().isBadRequest());

            long voteCount = voteRepository.count();
            assertThat(voteCount).isZero();
        }

        @Test
        @DisplayName("Должен вернуть 401 когда пользователь не аутентифицирован")
        void voteForChoice_NoAuthentication_Returns401() throws Exception {
            UUID optionId = UUID.randomUUID();
            VoteRequest voteRequest = new VoteRequest(choiceId, optionId);

            mockMvc.perform(post("/api/v1/choices/{id}/vote", choiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(voteRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpectAll(
                            jsonPath("$.message").value("Требуется аутентификация"),
                            jsonPath("$.timestamp").exists()
                    );

            long voteCount = voteRepository.count();
            assertThat(voteCount).isZero();
        }
    }

}
