package com.worch.tests.service;


import com.worch.model.entity.Channel;
import com.worch.model.entity.Choice;
import com.worch.model.entity.ChoiceOption;
import com.worch.model.entity.User;
import com.worch.model.enums.ChoiceStatus;
import com.worch.model.enums.Language;
import com.worch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class VoteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private ChoiceOptionRepository choiceOptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    private UUID userId;
    private Choice choice;
    private ChoiceOption choiceOption;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setLogin("test");
        user.setPassword("password");
        user.setFirstName("test");
        user.setLastName("test");
        user.setEmail("test");
        user.setPhone("123456789");
        user.setLanguage(Language.RUSSIAN);
        user.setBirthday(OffsetDateTime.now().minusYears(20));
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        userId = user.getId();

        Channel channel = new Channel();
        channel.setName("Test Channel");
        channel.setDescription("Test Channel");
        channel.setPassword("password");
        channel.setMembers(new HashSet<>());
        channel.setOwner(user);
        channel.setAgeRestricted(true);
        channel.setIsPrivate(false);
        channel.setCreatedAt(OffsetDateTime.now());
        channelRepository.save(channel);

        choice = new Choice();
        choice.setCreator(user);
        choice.setImageLink("http://example.com/image");
        choice.setTitle("test");
        choice.setDescription("description");
        choice.setCreatedAt(OffsetDateTime.now());
        choice.setPersonal(false);
        choice.setChannel(channel);
        choice.setStatus(ChoiceStatus.ACTIVE);
        choice = choiceRepository.save(choice);

        choiceOption = new ChoiceOption();
        choiceOption.setPosition(1);
        choiceOption.setName("test");
        choiceOption.setChoice(choice);

        choiceOption = choiceOptionRepository.save(choiceOption);
    }

    @Test
    void vote_withoutIdempotencyKey_secondRequestReturns409() throws Exception {
        String requestBody = """
                {
                  "choiceId": "%s",
                  "choiceOptionId": "%s"
                }
                """.formatted(choice.getId(), choiceOption.getId());

        mockMvc.perform(post("/api/v1/choices/vote")
                        .with(jwt().jwt(buildJwt(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Vote accepted\"}"));

        mockMvc.perform(post("/api/v1/choices/vote")
                        .with(jwt().jwt(buildJwt(userId)))
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    void vote_withSameIdempotencyKey_secondRequestReturnsSameResponse() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();

        String requestBody = """
                {
                  "choiceId": "%s",
                  "choiceOptionId": "%s"
                }
                """.formatted(choice.getId(), choiceOption.getId());

        String firstResponse = mockMvc.perform(post("/api/v1/choices/vote")
                        .with(jwt().jwt(buildJwt(userId)))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/v1/choices/vote")
                        .with(jwt().jwt(buildJwt(userId)))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(firstResponse, secondResponse);
        assertEquals(1, voteRepository.count());
        assertEquals(1, idempotencyRepository.count());
    }

    private Jwt buildJwt(UUID userId) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(userId.toString())
                .claim("sub", userId.toString())
                .build();
    }
}