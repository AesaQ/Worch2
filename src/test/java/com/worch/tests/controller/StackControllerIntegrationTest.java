package com.worch.tests.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.worch.model.entity.Stack;
import com.worch.model.entity.User;
import com.worch.repository.StackRepository;
import com.worch.tests.common.AbstractIntegrationTest;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class StackControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private StackRepository stackRepository;

  @BeforeEach
  void setUp() {
    stackRepository.deleteAll();

    for (int i = 1; i <= 3; i++) {
      Stack stack = new Stack();
      stack.setId(UUID.randomUUID());
      stack.setTitle("Stack " + i);
      stack.setDescription("Description " + i);
      stack.setOwner(User.builder().id(UUID.randomUUID()).build());
      stack.setIsQuiz(false);
      stack.setPublished(true);
      stack.setCreatedAt(OffsetDateTime.now());

      stackRepository.save(stack);
    }
  }

  @Test
  void getStacks_ShouldReturnPagedList() throws Exception {
    mockMvc.perform(get("/api/v1/stacks")
            .param("page", "0")
            .param("size", "5")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.content[0].title").value("Stack 1"));
  }
}