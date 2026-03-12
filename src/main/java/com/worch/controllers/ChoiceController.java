package com.worch.controllers;

import com.worch.mapper.ChoiceMapper;
import com.worch.model.dto.response.ChoiceDetailDto;
import com.worch.model.dto.response.ChoiceResponseDto;
import com.worch.model.dto.request.VoteRequest;
import com.worch.model.entity.Choice;
import com.worch.service.ChoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/choices")
@RequiredArgsConstructor
public class ChoiceController {
    private final ChoiceService choiceService;
    private final ChoiceMapper choiceMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChoiceResponseDto>> getChoices(
            @RequestParam(required = false) Optional<UUID> creatorId) {
        List<Choice> choices = choiceService.getChoices(creatorId);

        List<ChoiceResponseDto> responseDtos = choices.stream().map(choiceMapper::toDto).toList();
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    @GetMapping("/{choiceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChoiceDetailDto> getChoice(@PathVariable String choiceId) {
        return new ResponseEntity<>(choiceService.getChoiceDetail(UUID.fromString(choiceId)), HttpStatus.OK);
    }

    @PostMapping("/{choiceId}/close")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> closeChoice(@PathVariable String choiceId) {
        return new ResponseEntity<>(choiceService.closeChoice(UUID.fromString(choiceId)), HttpStatus.OK);
    }

    @PostMapping("/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> vote(@RequestBody VoteRequest voteRequest,
                                     @AuthenticationPrincipal Jwt jwt) {
        choiceService.vote(voteRequest, jwt);
        return ResponseEntity.ok().build();
    }
}