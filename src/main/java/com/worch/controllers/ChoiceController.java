package com.worch.controllers;

import com.worch.mapper.ChoiceMapper;
import com.worch.model.dto.response.ChoiceResponseDto;
import com.worch.model.dto.request.VoteRequest;
import com.worch.model.entity.Choice;
import com.worch.service.ChoiceService;
import com.worch.service.IdempotencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final HttpServletRequest httpServletRequest;
    private final IdempotencyService idempotencyService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChoiceResponseDto>> getChoices(@RequestParam(required = false) Optional<UUID> creatorId) {
        List<Choice> choices = choiceService.getChoices(creatorId);

        List<ChoiceResponseDto> responseDtos = choices.stream().map(choiceMapper::toDto).toList();
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

    @Operation(
            summary = "Vote for choice option",
            parameters = {
                    @Parameter(
                            name = "Idempotency-Key",
                            in = ParameterIn.HEADER,
                            description = "Unique key to make request idempotent",
                            required = false,
                            schema = @Schema(type = "string")
                    )
            }
    )
    @PostMapping("/vote")
    public ResponseEntity<String> vote(@RequestBody VoteRequest voteRequest,
                                       @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if(idempotencyKey == null) {
            return ResponseEntity.ok(choiceService.vote(voteRequest));
        }
        String response;
        String endpoint = httpServletRequest.getRequestURI();
        String cachedResponse = idempotencyService.checkIdempotencyKey(idempotencyKey, endpoint);
        if (cachedResponse != null) {
            response = cachedResponse;
        } else {
            response = choiceService.vote(voteRequest);
            idempotencyService.idempotencyKeyComplete(idempotencyKey, response);
        }
        return ResponseEntity.ok(response);
    }
}