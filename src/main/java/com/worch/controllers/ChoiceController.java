package com.worch.controllers;

import com.worch.mapper.ChoiceMapper;
import com.worch.model.dto.response.ChoiceResponseDto;
import com.worch.model.entity.Choice;
import com.worch.service.ChoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/choice")
@RequiredArgsConstructor
public class ChoiceController {
    private final ChoiceService choiceService;
    private final ChoiceMapper choiceMapper;

    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChoiceResponseDto>> getAllChoices(@RequestParam(required = false) Optional<UUID> creatorId) {
        List<Choice> choices;
        
        if (creatorId.isPresent()) {
            choices = choiceService.getAllChoicesByCreatorId(creatorId.get());
        } else {
            choices = choiceService.getAllChoices();
        }

        List<ChoiceResponseDto> responseDtos = choices.stream().map(choiceMapper::toDto).toList();
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

}