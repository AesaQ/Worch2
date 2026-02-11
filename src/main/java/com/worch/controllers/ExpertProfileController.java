package com.worch.controllers;

import com.worch.model.dto.response.ExpertProfileDto;
import com.worch.service.ExpertProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/experts")
@RequiredArgsConstructor
public class ExpertProfileController {
    private final ExpertProfileService expertProfileService;

    @GetMapping
    public ResponseEntity<List<ExpertProfileDto>> getExpertProfiles() {
        return ResponseEntity.ok(expertProfileService.getAllExpertProfiles());
    }
}
