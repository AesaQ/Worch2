package com.worch.controllers;

import com.worch.model.dto.request.VoteRequest;
import com.worch.service.ChoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/choices")
@RequiredArgsConstructor
public class ChoiceController {
    private final ChoiceService choiceService;

    @PostMapping("/vote")
    public ResponseEntity<?> vote(@RequestBody VoteRequest voteRequest) {
        choiceService.vote(voteRequest);
        return ResponseEntity.ok().build();
    }
}
