package com.worch.tests.controller;

import com.worch.controllers.ExpertProfileController;
import com.worch.model.dto.response.ExpertProfileDto;
import com.worch.service.ExpertProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ExpertProfileController.class)
public class ExpertProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpertProfileService expertProfileService;

    @Test
    void shouldReturnAllExpertProfiles() throws Exception {
        UUID id1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID id2 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
        UUID userId1 = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
        UUID userId2 = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");

        List<ExpertProfileDto> expertProfiles = List.of(
                new ExpertProfileDto(id1, userId1, "username1", true, 100, 1.0f),
                new ExpertProfileDto(id2, userId2, "username2", false, 200, 2.0f)
        );

        when(expertProfileService.getAllExpertProfiles()).thenReturn(expertProfiles);

        mockMvc.perform(get("/api/experts").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))

                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId1.toString()))
                .andExpect(jsonPath("$[0].username").value("username1"))
                .andExpect(jsonPath("$[0].isIncognito").value(true))
                .andExpect(jsonPath("$[0].price").value(100))
                .andExpect(jsonPath("$[0].rating").value(closeTo(1.0, 0.0001)))

                .andExpect(jsonPath("$[1].id").value(id2.toString()))
                .andExpect(jsonPath("$[1].userId").value(userId2.toString()))
                .andExpect(jsonPath("$[1].username").value("username2"))
                .andExpect(jsonPath("$[1].isIncognito").value(false))
                .andExpect(jsonPath("$[1].price").value(200))
                .andExpect(jsonPath("$[1].rating").value(closeTo(2.0, 0.0001)));

    }
}
